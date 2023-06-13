package com.bctech.hive.service.implementation;

import com.bctech.hive.utils.client.PayStackClient;
import com.bctech.hive.constant.TransactionType;
import com.bctech.hive.dto.request.BankTransferDto;
import com.bctech.hive.dto.request.PayStackPaymentRequest;
import com.bctech.hive.dto.request.PayStackTransferRecepientRequest;
import com.bctech.hive.dto.request.PayStackTransferRequest;
import com.bctech.hive.dto.response.*;
import com.bctech.hive.entity.User;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.service.EmailService;
import com.bctech.hive.service.PayStackService;
import com.bctech.hive.service.WalletService;
import com.bctech.hive.utils.AuthUtils;
import com.bctech.hive.utils.EmailTemplates;
import com.bctech.hive.utils.TransactionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.Principal;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class PayStackImpl implements PayStackService {

    @Value("${secret.key}")
    private  String PAY_STACK_SECRET_KEY;
    @Value("${paystack.url}")
    private  String PAY_STACK_BASE_URL;
    @Value("${paystack.verification.url}")
    private  String PAY_STACK_VERIFY_URL;
    private final AuthUtils authDetails;
    private final EmailService emailService;
    private final PayStackClient payStack;
    private final WalletService walletService;
    private final Gson gson;


    @Override
    public PayStackResponse initTransaction(Principal principal, PayStackPaymentRequest request) throws Exception {
        PayStackResponse initializeTransactionResponse;

        if (request.getAmount() <= 0) {
            throw new CustomException("Deposit must be greater than zero");
        }

        User user = authDetails.getAuthorizedUser(principal);

        if (user != null) {
            request.setEmail(user.getEmail());
        } else {
            throw new CustomException("No authenticated user found");
        }

        try {
            Gson gson = new Gson();
            StringEntity postingString = new StringEntity(gson.toJson(request));
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(PAY_STACK_BASE_URL);
            post.setEntity(postingString);
            post.addHeader("Content-type", "application/json");
            post.addHeader("Authorization", "Bearer " + PAY_STACK_SECRET_KEY);
            StringBuilder result = new StringBuilder();
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

            } else {
                throw new AuthenticationException("Error Occurred while initializing transaction");
            }
            ObjectMapper mapper = new ObjectMapper();

            initializeTransactionResponse = mapper.readValue(result.toString(), PayStackResponse.class);
        } catch (Exception ex) {
            throw new Exception("Failure initializing payStack transaction");
        }

        String reference = initializeTransactionResponse.getData().getReference();


        emailService.sendEmail(EmailTemplates.createPaymentVerificationCodeMail(user, reference));

        return initializeTransactionResponse;
    }

    @Override
    public VerifyTransactionResponse verifyPayment(String reference) {

        VerifyTransactionResponse payStackResponse;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(PAY_STACK_VERIFY_URL + reference);
            request.addHeader("Content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + PAY_STACK_SECRET_KEY);
            StringBuilder result = new StringBuilder();
            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

            } else {
                throw new CustomException("Error Occurred while connecting to PayStack URL");
            }
            ObjectMapper mapper = new ObjectMapper();


            payStackResponse = mapper.readValue(result.toString(), VerifyTransactionResponse.class);


        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        return payStackResponse;
    }

    @Override
    public List<ListBanksResponse> fetchBanks(String provider) {
        return switch (provider.toLowerCase()) {
            case "paystack" -> getListBanksResponses(payStack.listBanks());
            default -> throw new IllegalArgumentException("Invalid provider");
        };
    }
    private List<ListBanksResponse> getListBanksResponses(TransactionResponse response) {
        return gson.fromJson(gson.toJson(response.getData()), new TypeToken<List<ListBanksResponse>>() {
        }.getType());
    }
    @Override
    public Mono<TransactionResponse> transferFunds(BankTransferDto dto, String provider, User user) throws InterruptedException {

        Flux<TransactionResponse> responseFlux;

        try {
            switch (provider.toLowerCase()) {
                case "paystack" -> {
                    PayStackTransferRequest req = buildPayStackTransferRequest(dto, getRecipientCode(dto));
                    log.info("PayStack Transfer req: {}", req);
                    responseFlux = payStack.transferFunds(req, "Bearer " + PAY_STACK_SECRET_KEY);
                }

                default -> throw new IllegalArgumentException("Invalid provider");
            }
        } catch (Exception e) {
            throw new CustomException(e.getMessage());}

        walletService.withdrawFromWalletBalance(user, dto.getAmount(), TransactionType.WITHDRAW);

        return responseFlux.doOnNext(res -> log.info("res: {}", res))
                .next();

    }

    private String getRecipientCode(BankTransferDto dto) {

        PayStackTransferRecepientRequest recipientRequest = PayStackTransferRecepientRequest.builder()
                .accountNumber(dto.getBeneficiaryAccountNumber())
                .bankCode(dto.getBeneficiaryBankCode())
                .type("nuban")
                .build();

        Object recipient = payStack.createTransferRecipient(recipientRequest);

        var data = gson.fromJson(gson.toJson(recipient), JsonObject.class).get("data");

        return data.getAsJsonObject().get("recipient_code").getAsString();
    }
    private PayStackTransferRequest buildPayStackTransferRequest(BankTransferDto dto, String recipientCode) {
        String test = "";
        return PayStackTransferRequest.builder()
                .source("balance")
                .amount(dto.getAmount())
                .recipient(recipientCode)
                .reason(dto.getNarration())
                .reference(StringUtils.isBlank(dto.getTransactionReference()) ? TransactionUtil.generateUniqueRef() : dto.getTransactionReference())
                .build();
    }




}




