package com.iuniverse.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RefreshTokenRequest implements Serializable {
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}

