package com.bctech.hive.service.implementation;

import com.bctech.hive.dto.response.NotificationResponseDto;
import com.bctech.hive.entity.Notification;
import com.bctech.hive.entity.Task;
import com.bctech.hive.entity.TransactionLog;
import com.bctech.hive.entity.User;
import com.bctech.hive.constant.Role;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.repository.NotificationRepository;
import com.bctech.hive.repository.UserRepository;
import com.bctech.hive.service.EmailService;
import com.bctech.hive.service.NotificationService;
import com.bctech.hive.utils.EmailTemplates;
import com.bctech.hive.utils.EpochTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationServiceImpl implements NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationResponseDto taskCreationNotification(Task task, User user) {
        log.info("Sending Notification for task creation {}", user.getEmail());
        UUID userId = task.getTasker().getUser_id();
        User tasker = userRepository.findById(userId).orElseThrow(() ->
        {
            throw new CustomException("User not found");
        });
        if (!tasker.getRole().equals(Role.TASKER)) {
            throw new CustomException("User is not a Tasker");
        }

        Notification notification = Notification.builder()
                .user(tasker)
                .title(task.getJobType())
                .body("Your task has been successfully created, kindly await an acceptance")
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        return mapToNotificationResponse(savedNotification);
    }

    public NotificationResponseDto taskAcceptanceNotification(Task task, User user) {
        log.info("Sending Notification for task acceptance {}", user.getEmail());
        UUID userId = task.getDoer().getUser_id();
        User doer = userRepository.findById(userId).orElseThrow(() ->
        {
            throw new CustomException("User not found");
        });
        if (!doer.getRole().equals(Role.DOER)) {
            throw new CustomException("User is not a Doer");
        }
        Notification notification = Notification.builder()
                .user(doer)
                .title("Task Acceptance!!!")
                .createdAt(LocalDateTime.now())
                .body("Congratulations! You have successfully accepted a task with the following details: \n"
                        + "Task type: " + task.getJobType() + "\n"
                        + "Task description: " + task.getTaskDescription() + "\n"
                        + "Tasker Service Address: " + task.getTaskDeliveryAddress() + "\n"
                        + "Budget Rate: " + task.getBudgetRate() + "\n"
                        + "Tasker: " + task.getTasker().getFullName() + "\n"
                        + "Thank you for using Hive!")
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        return mapToNotificationResponse(savedNotification);
    }

    public NotificationResponseDto doerAcceptanceNotification(Task task, User user) {
        log.info("Sending Notification to user Tasker {} after doer accepts the task", task.getTasker().getFullName());
        UUID userId = task.getTasker().getUser_id();
        User tasker = userRepository.findById(userId).orElseThrow(() ->
        {
            throw new CustomException("User not found");
        });
        if (!tasker.getRole().equals(Role.DOER)) {
            throw new CustomException("User is not a Doer");
        }
        Notification notification = Notification.builder()
                .user(tasker)
                .title("Task Accepted!")
                .createdAt(LocalDateTime.now())
                .body("Congratulations! Your task has been successfully accepted by " + task.getDoer().getFullName() + "\n"
                        + "Task Details: \n"
                        + "Task type: " + task.getJobType() + "\n"
                        + "Tasker Service Address: " + task.getTaskDeliveryAddress() + "\n"
                        + "Budget Rate: " + task.getBudgetRate() + "\n"
                        + "Thank you for using Hive!")
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        return mapToNotificationResponse(savedNotification);
    }

    @Override
    public void walletActivityNotification(User user, TransactionLog transactionLog) throws IOException {
        log.info("Sending Wallet Activity Notification to user  {} ", user.getFullName());

        switch (transactionLog.getTransactionType()) {
            case DEPOSIT -> {

                String title = "Wallet Deposit!";
                String body = "Your wallet has been credited with " + transactionLog.getAmount() + "\n"
                        + "Thank you for using Hive!";
                makeNotificationToSystemAndEmail(user, title, body);
            }
            case WITHDRAW -> {
                String title = "Wallet Debit!";
                String body = "Your wallet has been debited with " + transactionLog.getAmount() + "\n"
                        + "Thank you for using Hive!";
                makeNotificationToSystemAndEmail(user, title, body);
            }
            case ESCROW -> {
                String title = "Escrow Transfer!";
                String body = "Your wallet has been debited with " + transactionLog.getAmount() + "\n"
                        + "Thank you for using Hive!";
                makeNotificationToSystemAndEmail(user, title, body);
            }
            case REFUND -> {
                String title = "Escrow Refund!";
                String body = "Your wallet has been credited with " + transactionLog.getAmount() + "\n"
                        + "Thank you for using Hive!";
                makeNotificationToSystemAndEmail(user, title, body);
            }
            default -> throw new CustomException("Transaction Type not found");
        }
    }

    @Override
    public void taskCompletionNotification(Task task, User user) throws IOException {

        //make notification to tasker
        String title = "Task Completed!";
        String body = "Your task with type: " + task.getJobType() + " has been successfully completed by " + task.getDoer().getFullName()
                       + "\n" + "Kindly Approve!  \n";
        makeNotificationToSystemAndEmail(task.getTasker(), title, body);

        //make notification to doer
        String body2 = "Your task with type: " + task.getJobType() + "has been successfully completed. Kindly wait for the tasker to approve your work";
        makeNotificationToSystemAndEmail(task.getDoer(), title, body2);

    }
        @Override
        public void taskerApprovalNotification (Task task, User user) throws IOException {
            //make notification to tasker
            String title = "Task Approved!";
            String body = "The task with type: " + task.getJobType() + " has been successfully approved,thank you for using Hive!";
            makeNotificationToSystemAndEmail(task.getTasker(), title, body);
            //make notification to doer
            makeNotificationToSystemAndEmail(task.getDoer(), title, body);
        }


        public NotificationResponseDto walletFundingNotification (User user, BigDecimal amount){
            log.info("Sending Wallet Funding Notification to user  {} ", user.getFullName());

            Notification notification = Notification.builder()
                    .user(user)
                    .title("Wallet Funded!")
                    .createdAt(LocalDateTime.now())
                    .body("Congratulations! Your wallet has been successfully funded with " + amount + "\n"
                            + "Thank you for using Hive!")
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            return mapToNotificationResponse(savedNotification);
        }

        @Override
        public List<NotificationResponseDto> getAllNotificationOfUser (String email){
            User user = userRepository.findByEmail(email).orElseThrow(() -> {
                throw new CustomException("User with the email: " + email + " was not found");
            });
            List<Notification> notifications = notificationRepository.findAllByUserOrderByCreatedAtDesc(user);
            List<NotificationResponseDto> notificationResponseDtos = new ArrayList<>();

            for (Notification notification : notifications) {
                NotificationResponseDto notificationResponseDto = new ModelMapper().map(notification, NotificationResponseDto.class);
                notificationResponseDto.setUserId(user.getUser_id().toString());

//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
//            LocalDateTime datetime = LocalDateTime.parse(notification.getCreatedAt(), formatter);


                notificationResponseDto.setElapsedTime(EpochTime.getElapsedTime(notification.getCreatedAt()));
                notificationResponseDtos.add(notificationResponseDto);
            }
            return notificationResponseDtos;
        }


        private NotificationResponseDto mapToNotificationResponse (Notification notification){
            return NotificationResponseDto.builder()
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .createdAt(notification.getCreatedAt().toString())
                    .build();
        }


        private void makeNotificationToSystemAndEmail (User user, String title, String body) throws IOException {
            notificationRepository.save(Notification.builder()
                    .user(user)
                    .title(title)
                    .createdAt(LocalDateTime.now())
                    .body(body)
                    .build());
            emailService.sendEmail(EmailTemplates.notificationActivityEmail(user, title, body));
        }

    }
