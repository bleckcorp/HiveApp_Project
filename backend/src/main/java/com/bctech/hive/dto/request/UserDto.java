package com.bctech.hive.dto.request;

import com.bctech.hive.entity.Address;
import com.bctech.hive.entity.Task;
import com.bctech.hive.constant.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private String fullName;
    @Email
    private String email;
    private String phoneNumber;
    private String validId;
    private Address address;
    private String password;
    private Boolean isVerified;
    private Role role;
    private List<Task> tasks;
}
