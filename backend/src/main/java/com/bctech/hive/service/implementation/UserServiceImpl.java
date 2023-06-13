package com.bctech.hive.service.implementation;


import com.bctech.hive.dto.request.UserRegistrationRequestDto;
import com.bctech.hive.dto.response.UserRegistrationResponseDto;
import com.bctech.hive.entity.*;
import com.bctech.hive.exceptions.ResourceNotFoundException;
import com.bctech.hive.repository.*;
import com.bctech.hive.service.UserService;
import com.bctech.hive.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.bctech.hive.entity.User;
import com.bctech.hive.constant.Role;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.repository.UserRepository;
import com.bctech.hive.utils.event.RegistrationCompleteEvent;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder;

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final WalletService walletService;
    private final ModelMapper modelMapper;

    @Value("${base.url}")
    private  String baseUrl;

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {

        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).get().getUser());

    }

    @Override
    @Transactional
    public UserRegistrationResponseDto registerUser(UserRegistrationRequestDto registrationRequestDto, HttpServletRequest request) {
        log.info("register user and create account");

        if (doesUserAlreadyExist(registrationRequestDto.getEmail())) {
            throw new CustomException("User already exist", HttpStatus.FORBIDDEN);
        }
        User newUser = saveNewUser(registrationRequestDto);
        //reate a wallet account for both doer and tasker

        walletService.createWallet(newUser);

        // generateToken and Save to token repo, send email also
        eventPublisher.publishEvent(new RegistrationCompleteEvent(
                newUser,baseUrl
        ));
        return modelMapper.map(newUser, UserRegistrationResponseDto.class);
    }

    @Override
    public Boolean validateRegistrationToken(String token) {
        Boolean status = false;
        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() -> new CustomException("Token does not Exist : "+ token, HttpStatus.BAD_REQUEST));
        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();
        // check if user is already verified
        if (user.getIsVerified()) {
            verificationTokenRepository.delete(verificationToken);
            throw new CustomException("User is already verified", HttpStatus.BAD_REQUEST);
        }
        // check if token is expired
        if((verificationToken.getExpirationTime().getTime() - cal.getTime().getTime()) <= 0){
            throw new CustomException("Token has expired", HttpStatus.BAD_REQUEST);

        }
        // check if token is valid
        if (verificationToken.getExpirationTime().getTime() - cal.getTime().getTime() > 0 ) {
            user.setIsVerified(true);
            log.info("i have verifed token {}", verificationToken);
            userRepository.save(user);
            // activate the wallet
            walletService.activateWallet(user);
            verificationTokenRepository.delete(verificationToken);
            status = true;
        }
        return status;
    }

    @Override
    public  String generateVerificationToken(User user) {
        log.info("inside generateVerificationToken, generating token for {}", user.getEmail());
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(token);
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + 900000);
        verificationToken.setExpirationTime(expirationDate);

        log.info("Saving token to database {}", token);
        verificationTokenRepository.save(verificationToken);
        return token;
    }
    @Override
    public VerificationToken generateNewToken(User user) {
        // does the user have a saved (old)token?
        VerificationToken verificationToken = verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Token does not Exist", HttpStatus.BAD_REQUEST));
        verificationToken.setToken(UUID.randomUUID().toString());
        Date currentDate = new Date();
        Date expirationDate = new Date(currentDate.getTime() + 900000);
        verificationToken.setExpirationTime(expirationDate);
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("userService loadUserByUserName - email :: [{}] ::", email);
        log.info("User ==> [{}]", userRepository.findByEmail(email));
        User user = (User) userRepository.findByEmail(email)
                .orElseThrow(
                        () -> {
                            throw new ResourceNotFoundException("user does not exist");
                        }
                );
        Collection<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);

    }
    //HELPER METHODS

    private User saveNewUser(UserRegistrationRequestDto registrationRequestDto) {
        User newUser = new User();
        Role role = registrationRequestDto.getRole();
        BeanUtils.copyProperties(registrationRequestDto, newUser);
        log.info("user has a role of {}",registrationRequestDto.getRole().toString());
        newUser.addRole(role);
        log.info("user now has a role of {}",newUser.getRoles().toString());
        newUser.setPassword(passwordEncoder.encode(registrationRequestDto.getPassword()));


        return userRepository.save(newUser);
    }

    private boolean doesUserAlreadyExist(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

}
