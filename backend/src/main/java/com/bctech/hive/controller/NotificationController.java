package com.bctech.hive.controller;

import com.bctech.hive.constant.ResponseStatus;
import com.bctech.hive.dto.response.AppResponse;
import com.bctech.hive.dto.response.NotificationResponseDto;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user-notifications")
    public ResponseEntity<AppResponse<List<NotificationResponseDto>>> getAllNotifications(final Principal principal) {
        final String email = principal.getName();
        try {
            final List<NotificationResponseDto> notifications = notificationService.getAllNotificationOfUser(email);
            return ResponseEntity.status(200).body(AppResponse.<List<NotificationResponseDto>>builder().statusCode(ResponseStatus.SUCCESSFUL.getCode()).isSuccessful(true).message("success").result(notifications).build());
        } catch(CustomException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(AppResponse.<List<NotificationResponseDto>>builder().statusCode(ResponseStatus.NOT_FOUND.getCode()).result(null).isSuccessful(false).message(e.getMessage()).build());
        }
    }
}
