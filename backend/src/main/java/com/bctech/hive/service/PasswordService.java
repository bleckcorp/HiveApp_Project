package com.bctech.hive.service;

import com.bctech.hive.dto.request.ResetPasswordDto;
import com.bctech.hive.entity.User;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

public interface PasswordService {
    void createPasswordResetTokenForUser(User user, String token);
    String passwordResetTokenMail(User user, String applicationUrl, String token);
    String applicationUrl(HttpServletRequest request);
    void sendEmail(String recipientEmail, String link)
            throws UnsupportedEncodingException, MessagingException;
    String validatePasswordResetToken(String token);
    void changePassword(User user, ResetPasswordDto passwordDto);
}