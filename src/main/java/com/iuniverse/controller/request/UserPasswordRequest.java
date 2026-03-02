package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class UserPasswordRequest {
    @NotNull(message = "Id is required")
    private Long id;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "New password is required")
    private String newPassword;
}
