package com.bctech.hive.service;

import com.bctech.hive.constant.TransactionType;
import com.bctech.hive.dto.response.TransactionLogResponse;
import com.bctech.hive.dto.response.TransactionResponse;
import com.bctech.hive.entity.Task;
import com.bctech.hive.entity.User;

import java.math.BigDecimal;
import com.bctech.hive.dto.response.WalletResponseDto;
import com.bctech.hive.entity.Wallet;

import java.security.Principal;
import java.util.List;

public interface WalletService {



    boolean creditDoerWallet(User doer, BigDecimal creditAmount);

    WalletResponseDto getWalletByUser(Principal principal);

    void withdrawFromWalletBalance(User user, BigDecimal amount, TransactionType transactionType);

    boolean fundTaskerWallet(User tasker, BigDecimal amountToFund, TransactionType transactionType);

    boolean debitTaskerWalletToEscrow(Wallet wallet, BigDecimal amount);

    boolean refundTaskerFromEscrowWallet(Task taskToCancel);

    void createWallet(User user);

    void activateWallet(User user);

    List<TransactionLogResponse> getWalletHistory(Principal principal);
}
