package com.bctech.hive.utils.event;

import com.bctech.hive.entity.TransactionLog;
import com.bctech.hive.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;


@Getter
@Setter
@Builder
public class SuccessfulCreditEvent extends ApplicationEvent {
    private User user;
    private TransactionLog transactionLog;


    public SuccessfulCreditEvent(User user, TransactionLog transactionLog) {
        super(user);
        this.user = user;
        this.transactionLog = transactionLog;
    }
}
