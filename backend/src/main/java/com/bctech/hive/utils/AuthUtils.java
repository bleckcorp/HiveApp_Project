package com.bctech.hive.utils;

import com.bctech.hive.entity.User;
import com.bctech.hive.exceptions.CustomException;
import com.bctech.hive.repository.UserRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Component
@NoArgsConstructor
public class AuthUtils {


    private UserRepository userRepository;

    @Autowired
    public AuthUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthorizedUser(Principal principal) {
        if (principal != null) {
            var currentUser = principal;
            return userRepository.findByEmail(currentUser.getName()).orElseThrow(
                    () -> new CustomException(currentUser.getName())
            );
        } else {
            return null;
        }
    }

}
