package com.bctech.hive.service;

import com.bctech.hive.dto.response.NotificationResponseDto;
import com.bctech.hive.entity.Notification;
import com.bctech.hive.entity.Task;
import com.bctech.hive.entity.TransactionLog;
import com.bctech.hive.entity.User;

import java.io.IOException;
import java.util.List;

public interface NotificationService {

    NotificationResponseDto taskCreationNotification(Task task, User user);
    NotificationResponseDto taskAcceptanceNotification(Task task, User user);
    NotificationResponseDto doerAcceptanceNotification(Task task, User user);

    void walletActivityNotification(User user, TransactionLog transactionLog) throws IOException;

    void taskCompletionNotification(Task task, User user) throws IOException;

    void taskerApprovalNotification( Task task, User user) throws IOException;
    List<NotificationResponseDto> getAllNotificationOfUser(String email);
}
