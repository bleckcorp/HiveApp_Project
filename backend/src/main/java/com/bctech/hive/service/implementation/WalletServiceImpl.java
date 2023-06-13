package com.bctech.hive.service.implementation;

import com.bctech.hive.constant.TransactionStatus;
import com.bctech.hive.constant.TransactionType;
import com.bctech.hive.entity.*;
import com.bctech.hive.dto.response.WalletResponseDto;
import com.bctech.hive.constant.Role;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.exceptions.ResourceNotFoundException;
import com.bctech.hive.repository.EscrowWalletRepository;
import com.bctech.hive.repository.TransactionLogRepository;
import com.bctech.hive.repository.UserRepository;
import com.bctech.hive.repository.WalletRepository;
import com.bctech.hive.service.NotificationService;
import com.bctech.hive.service.WalletService;
import com.bctech.hive.utils.event.SuccessfulCreditEvent;
import com.bctech.hive.utils.event.WalletFundingEvent;
import com.bctech.hive.dto.response.TransactionLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final TransactionLogRepository transactionLogRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final NotificationService notificationService;

    private final EscrowWalletRepository escrowWalletRepository;

    private final ModelMapper modelMapper;

    @Override
    public boolean creditDoerWallet(User doer, BigDecimal creditAmount){

        log.info("Crediting doer wallet{}", doer.getFullName()) ;
        //check role of user
        if (!doer.getRole().equals(Role.DOER)) {
            throw new CustomException("User is not a doer");
        }
        else {
            //check if user has a wallet
            Wallet wallet =  walletRepository.findByUser(doer).orElseThrow(() -> new CustomException("User does not have a wallet"));
            log.info("I found wallet balance of {}", wallet.getAccountBalance()) ;

            if (wallet.getAccountBalance() == null) {wallet.setAccountBalance(creditAmount);}

            //credit wallet
            else { wallet.setAccountBalance(wallet.getAccountBalance().add(creditAmount));}
            log.info("NOW I found wallet balance of {}", wallet.getAccountBalance()) ;

            TransactionLog transactionLog = new TransactionLog ();
            transactionLog.setAmount(creditAmount);
            transactionLog.setUser(doer);
            transactionLog.setTransactionType(TransactionType.DEPOSIT);
            transactionLog.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionLog.setTransactionDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));

            transactionLogRepository.save(transactionLog);
            eventPublisher.publishEvent(new SuccessfulCreditEvent(doer, transactionLog));
            eventPublisher.publishEvent(new WalletFundingEvent(this, doer, creditAmount));

            try {
                notificationService.walletActivityNotification(doer, transactionLog);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;

        }

    }


    @Override
    public void withdrawFromWalletBalance(User user, BigDecimal amount, TransactionType transactionType) {
        Wallet wallet = walletRepository.findByUser(user).orElseThrow(() -> new CustomException("User does not have a wallet"));
        if (wallet.getAccountBalance().compareTo(amount) < 0) {
            throw new CustomException("Insufficient funds");
        }
        wallet.setAccountBalance(wallet.getAccountBalance().subtract(amount));
        walletRepository.save(wallet);
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setAmount(amount);
        transactionLog.setUser(user);
        transactionLog.setTransactionType(transactionType);
        transactionLog.setTransactionStatus(TransactionStatus.SUCCESS);
        transactionLog.setTransactionDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));

        try {
            notificationService.walletActivityNotification(user, transactionLog);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        transactionLogRepository.save(transactionLog);
    }
    @Override
    public WalletResponseDto getWalletByUser(Principal principal) {

        //get the user from the princial
        User user = userRepository.findByEmail(principal.getName()).get();
        Wallet getWallet = walletRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Wallet not found"));
        return new WalletResponseDto(getWallet.getAccountBalance());
    }

    @Override
    public boolean fundTaskerWallet(User tasker, BigDecimal amountToFund, TransactionType transactionType) {
        log.info("Crediting tasker wallet{}", tasker.getFullName()) ;
        //check role of user
        if (!tasker.getRole().equals(Role.TASKER)) {
            throw new CustomException("User is not a tasker");
        }
        else {
            //check if user has a wallet
            Wallet wallet =  walletRepository.findByUser(tasker).orElseThrow(() -> new CustomException("User does not have a wallet"));
            log.info("I found wallet balance of {}", wallet.getAccountBalance()) ;

            if (wallet.getAccountBalance() == null) {wallet.setAccountBalance(amountToFund);}

            //credit wallet
            else { wallet.setAccountBalance(wallet.getAccountBalance().add(amountToFund));}
            log.info("NOW I found wallet balance of {}", wallet.getAccountBalance()) ;

            TransactionLog transactionLog = new TransactionLog ();
            transactionLog.setAmount(amountToFund);
            transactionLog.setUser(tasker);
            transactionLog.setTransactionType(transactionType);
            transactionLog.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionLog.setTransactionDate(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(LocalDateTime.now()));

            transactionLogRepository.save(transactionLog);
            walletRepository.save(wallet);
            eventPublisher.publishEvent(new SuccessfulCreditEvent(tasker, transactionLog));
            eventPublisher.publishEvent(new WalletFundingEvent(this, tasker, amountToFund));
            try {
                notificationService.walletActivityNotification(tasker, transactionLog);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;

        }
    }

    @Override
    public boolean debitTaskerWalletToEscrow(Wallet wallet, BigDecimal amount) {
        log.info("Debiting tasker wallet{}", wallet.getUser().getFullName()) ;
        //check role of user
        if (!wallet.getUser().getRole().equals(Role.TASKER)) {
            throw new CustomException("User is not a tasker role");
        }
        else {
            withdrawFromWalletBalance(wallet.getUser(), amount, TransactionType.ESCROW);
            return true;

        }
    }

    @Override
    public boolean refundTaskerFromEscrowWallet(Task task) {
        User tasker = task.getTasker();
        EscrowWallet escrowWallet = task.getEscrowWallet();
        if ( fundTaskerWallet(tasker, escrowWallet.getEscrowAmount(),TransactionType.REFUND)) {
            escrowWallet.setEscrowAmount(new BigDecimal(0));
            escrowWalletRepository.save(escrowWallet);
            return true;
        }
        return false;
    }

    @Override
    public void createWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        walletRepository.save(wallet);
    }

    @Override
    public void activateWallet(User user) {
        Wallet wallet = walletRepository.findByUser(user).orElseThrow( () -> new CustomException("User does not have a wallet"));
        wallet.setActivated(true);
        walletRepository.save(wallet);

    }

    @Override
    public List<TransactionLogResponse> getWalletHistory(Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        List<TransactionLog> transactionLogs = transactionLogRepository.findAllByUserAndTransactionStatusOrderByUpdatedDate(user, TransactionStatus.SUCCESS);
        List<TransactionLogResponse> list= transactionLogs.stream().map(transactionLog -> modelMapper.map(transactionLog, TransactionLogResponse.class)).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }
}
