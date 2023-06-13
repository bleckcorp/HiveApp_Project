package com.bctech.hive.utils.event.listeners;

import com.bctech.hive.entity.Task;
import com.bctech.hive.entity.TransactionLog;
import com.bctech.hive.entity.User;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.service.implementation.NotificationServiceImpl;
import com.bctech.hive.utils.event.WalletFundingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Log4j2
public class WalletFundingEventListener implements ApplicationListener<WalletFundingEvent> {

    private final NotificationServiceImpl notificationService;

    @Override
    public void onApplicationEvent(WalletFundingEvent event ) {
        var user = event.getUser();
        BigDecimal amount = event.getAmount();

        try {
            notificationService.walletFundingNotification(user, amount);
        } catch (CustomException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
