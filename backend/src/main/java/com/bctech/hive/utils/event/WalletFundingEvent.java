package com.bctech.hive.utils.event;

import com.bctech.hive.entity.Task;
import com.bctech.hive.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class WalletFundingEvent extends ApplicationEvent {

    private final BigDecimal amount;
    private final User user;

    public WalletFundingEvent(Object source, User user, BigDecimal amount) {
        super(source);
        this.user = user;
        this.amount = amount;
    }
}
