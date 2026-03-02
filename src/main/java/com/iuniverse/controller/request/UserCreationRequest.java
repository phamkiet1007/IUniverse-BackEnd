package com.iuniverse.controller.request;

import com.iuniverse.common.Gender;
import com.iuniverse.common.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@ToString
public class UserCreationRequest implements Serializable {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;
    private Gender gender;
    private Date birthday;
    private String username;
    private String phoneNumber;

    @Email(message = "Email invalid")
    private String email;
    private AddressRequest address;
    private UserType userType;
    private String status;
}
