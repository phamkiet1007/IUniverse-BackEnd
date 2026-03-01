package com.iuniverse.controller.request;

import com.iuniverse.common.Gender;
import com.iuniverse.common.UserType;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

@Getter
@ToString
public class UserUpdateRequest {
    private Long id;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Date birthday;
    private String username;
    private String phoneNumber;
    private String email;
    private AddressRequest address;
    private UserType userType;
    private String status;
}
