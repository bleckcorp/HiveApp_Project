package com.bctech.hive.service;

import com.bctech.hive.dto.request.BankTransferDto;
import com.bctech.hive.dto.request.PayStackPaymentRequest;
import com.bctech.hive.dto.response.ListBanksResponse;
import com.bctech.hive.dto.response.PayStackResponse;
import com.bctech.hive.dto.response.TransactionResponse;
import com.bctech.hive.dto.response.VerifyTransactionResponse;
import com.bctech.hive.entity.User;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

public interface PayStackService {

    PayStackResponse initTransaction(Principal principal, PayStackPaymentRequest request) throws Exception;

    VerifyTransactionResponse verifyPayment(String reference) throws IOException;

    List<ListBanksResponse> fetchBanks(String provider);

    Mono<TransactionResponse> transferFunds(BankTransferDto dto, String provider, User user) throws InterruptedException;


}
