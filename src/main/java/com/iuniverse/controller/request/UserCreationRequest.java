package com.iuniverse.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iuniverse.common.Gender;
import com.iuniverse.common.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
public class UserCreationRequest implements Serializable {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private Gender gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date birthday;

    @NotBlank(message = "Username is required")
    private String username;

    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    private AddressRequest address;

    @NotNull(message = "Role is required (ADMIN, TEACHER, STUDENT)")
    private Role role;

    //Bắt buộc phải gửi nếu role = STUDENT
    private String studentCode;

    //Gửi kèm nếu role = TEACHER
    private String department;

}