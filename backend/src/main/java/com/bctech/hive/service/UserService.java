package com.bctech.hive.service;
import com.bctech.hive.dto.request.UserRegistrationRequestDto;
import com.bctech.hive.dto.response.UserRegistrationResponseDto;
import com.bctech.hive.entity.User;

import java.security.Principal;
import java.util.Optional;
import com.bctech.hive.entity.VerificationToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    Optional<User> findUserByEmail(String email);

    Optional<User> getUserByPasswordResetToken(String token);

    @Transactional
    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto registrationRequestDto, HttpServletRequest request);

    Boolean validateRegistrationToken(String token);

    String generateVerificationToken(User user);
    VerificationToken generateNewToken(User user);

}
