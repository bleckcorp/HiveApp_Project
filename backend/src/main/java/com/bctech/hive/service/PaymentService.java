package com.bctech.hive.service;

import com.bctech.hive.dto.request.FundWalletRequest;
import com.bctech.hive.dto.response.PayStackResponse;

import com.bctech.hive.dto.response.VerifyTransactionResponse;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

public interface PaymentService {



    PayStackResponse initiatePaymentAndSaveToPaymentLog(FundWalletRequest taskerPaymentRequest, Principal principal) throws Exception;

    VerifyTransactionResponse verifyAndCompletePayment(String reference , Principal principal) throws Exception;


    VerifyTransactionResponse verifyAndCompletePayment(String reference) throws Exception;
}
