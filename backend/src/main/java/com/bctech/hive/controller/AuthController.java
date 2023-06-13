package com.bctech.hive.controller;


import com.bctech.hive.dto.request.UserRegistrationRequestDto;
import com.bctech.hive.dto.response.AppResponse;
import com.bctech.hive.dto.response.UserRegistrationResponseDto;
import com.bctech.hive.entity.User;
import com.bctech.hive.entity.VerificationToken;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.exceptions.ResourceNotFoundException;
import com.bctech.hive.repository.UserRepository;
import com.bctech.hive.utils.JwtUtils;
import com.bctech.hive.dto.response.TokenResponse;
import com.bctech.hive.dto.request.LoginRequest;
import com.bctech.hive.service.EmailService;
import com.bctech.hive.service.UserService;
import com.bctech.hive.utils.EmailTemplates;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;

import static com.bctech.hive.constant.SecurityConstants.PASSWORD_NOT_MATCH_MSG;
import static com.bctech.hive.utils.StringUtil.doesBothStringMatch;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailService emailService;

    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody LoginRequest request) {
        log.info("controller login: login user :: [{}] ::", request.getEmail());
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        if (authentication.isAuthenticated()) {
            User currentUser = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("user does not exist"));

            TokenResponse tokenResponse = JwtUtils.generateToken(authentication, currentUser);
            return ResponseEntity.status(200).body(AppResponse.builder().statusCode("00").isSuccessful(true).result(tokenResponse).message("Authenticated").build());
        } else {
            throw new UsernameNotFoundException("invalid user request !");
        }
    }

    @PostMapping(path = "/register")
    public ResponseEntity<AppResponse<?>> registerUser(@RequestBody @Valid UserRegistrationRequestDto registrationRequestDto, HttpServletRequest request) {
        log.info("controller register: register user :: [{}] ::", registrationRequestDto.getEmail());
        validateUserRegistration(registrationRequestDto);
        UserRegistrationResponseDto response = userService.registerUser(registrationRequestDto, request);
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/auth/register").toUriString());
        return ResponseEntity.created(uri).body(AppResponse.buildSuccess(response));
    }

    private void validateUserRegistration(UserRegistrationRequestDto registrationRequestDto) {
        log.info("validating user registration request for email :: {}", registrationRequestDto.getEmail());
        if (!doesBothStringMatch(registrationRequestDto.getConfirmPassword(), registrationRequestDto.getPassword())) {
            throw new CustomException(PASSWORD_NOT_MATCH_MSG, HttpStatus.BAD_REQUEST);
        }
        List<String> roleEnum = List.of("TASKER", "DOER");

        String role = String.valueOf(registrationRequestDto.getRole());
        if (role != null) {
            role = role.trim().toUpperCase();
            if (!roleEnum.contains(role)) {
                throw new CustomException("Invalid role, Options includes: TASKER, DOER");
            }
        }
        log.info("successful validation for user registration request for email :: {}", registrationRequestDto.getEmail());
    }


    @GetMapping("/verifyRegistration")
    public ResponseEntity<AppResponse<Object>> validateRegistrationToken(@RequestParam String token){
        log.info("controller register: validateRegistrationToken {}", token);

        token = token.replace("[", "").replace("]","");
        boolean isValid = userService.validateRegistrationToken(token);

        return isValid ? ResponseEntity.ok().body(AppResponse.buildSuccess("User verified successfully"))
                :  ResponseEntity.ok().body(
                AppResponse.<Object>builder()
                        .message("USer failed to verify")
                        .statusCode(HttpStatus.BAD_REQUEST.value()+"")
                        .build());
    }

    @GetMapping("/resendVerificationToken")
    public ResponseEntity<AppResponse<?>> resendVerificationToken(Principal principal, HttpServletRequest request) throws IOException {

        User user= getLoggedInUser(principal);

        ResponseEntity<AppResponse<?>> response = checkVerifiedStatusOfUser(user);
        if (response != null) return response;

        VerificationToken verificationToken = userService.generateNewToken(user);
        emailService.sendEmail(EmailTemplates.createVerificationEmail(user, verificationToken.getToken(),  getVerificationUrl(request) ));

        return ResponseEntity.ok().body(
                AppResponse.<Object>builder()
                        .message("Verification email sent successfully")
                        .statusCode("200")
                        .build());
    }

    private static String getVerificationUrl(HttpServletRequest request) {
        String verificationUrl = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/auth";
        return verificationUrl;
    }

    private User getLoggedInUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("USer not found"));
    }

    private ResponseEntity<AppResponse<?>> checkVerifiedStatusOfUser(User user) {
        if (user.getIsVerified()) {
            return ResponseEntity.ok().body(
                    AppResponse.<Object>builder()
                            .message("User is already verified")
                            .statusCode(HttpStatus.FORBIDDEN.toString())
                            .build());
        }
        return null;
    }

}
