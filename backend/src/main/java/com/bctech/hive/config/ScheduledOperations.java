package com.bctech.hive.config;

import com.bctech.hive.constant.TransactionStatus;
import com.bctech.hive.entity.PaymentLog;
import com.bctech.hive.entity.Task;
import com.bctech.hive.constant.Status;
import com.bctech.hive.repository.*;
import com.bctech.hive.service.PaymentService;
import com.bctech.hive.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class ScheduledOperations {

    private final PaymentLogRepository paymentLogRepository;
    private final TaskRepository taskRepository;
    private final PaymentService paymentService;
    private final WalletService walletService;



    @Scheduled(cron = "0 */5 * * * *") // Runs every 5 minutes
    public void checkTaskExpiry() {
        log.info("Checking for expired tasks...");
        LocalDateTime now = LocalDateTime.now();
        List<Task> expiredTasks = taskRepository.findAllByTaskDurationLessThanAndStatus(now, Status.NEW);
        // Process the expired tasks by changing status and refunding the tasker
        expiredTasks.forEach(task -> {
            task.setStatus(Status.EXPIRED);
            taskRepository.save(task);
            walletService.refundTaskerFromEscrowWallet(task);
        });
    }

    @Scheduled(cron = "0 * * * * *")// Runs every 1 minute
    public void verifyPendingPaystackTransactions(){

        log.info("Verifying pending transactions...");

        // get all pending payment logs
        List<PaymentLog> pendingPaymentLogs = paymentLogRepository.findAllByTransactionStatus(TransactionStatus.PENDING);
        pendingPaymentLogs.forEach(paymentLog -> {
                    //verify status
                    try {
                        paymentService.verifyAndCompletePayment(paymentLog.getTransactionReference());
                    } catch (Exception e) {
                        log.info("Transaction verification failed for transaction reference: " + paymentLog.getTransactionReference());
                    }
                }
        );
    }
}
