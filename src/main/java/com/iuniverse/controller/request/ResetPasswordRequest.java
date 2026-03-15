package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
public class ResetPasswordRequest implements Serializable {
    @NotBlank(message = "secretKey must be not blank")
    private String secretKey;

    @NotBlank(message = "password must be not blank")
    private String password;

    @NotBlank(message = "confirmPassword must be not blank")
    private String confirmPassword;
}
