package com.bctech.hive.dto.response;

import com.bctech.hive.entity.Address;
import com.bctech.hive.constant.Role;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponseDto {

    private String fullName;

    private String email;

    private String phoneNumber;

    private Address address;

    private Boolean isVerified;

    private Role role;

}
