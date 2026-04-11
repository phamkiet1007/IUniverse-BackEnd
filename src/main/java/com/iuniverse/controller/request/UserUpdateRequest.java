package com.iuniverse.controller.request;

import com.iuniverse.common.Gender;
import com.iuniverse.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

@Getter
@ToString
public class UserUpdateRequest {
    @NotNull(message = "Id is required")
    private Long id;

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
    private Role role;
    private String status;
}
