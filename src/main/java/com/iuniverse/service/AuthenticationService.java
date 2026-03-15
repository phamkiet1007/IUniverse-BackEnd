package com.iuniverse.service;

import com.iuniverse.controller.request.LogoutRequest;
import com.iuniverse.controller.request.RefreshTokenRequest;
import com.iuniverse.controller.request.ResetPasswordRequest;
import com.iuniverse.controller.request.SignInRequest;
import com.iuniverse.controller.response.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

public interface AuthenticationService {
    TokenResponse getAccessToken(SignInRequest request);

    TokenResponse getRefreshToken(RefreshTokenRequest request);

    String logout(HttpServletRequest request, LogoutRequest logoutRequest);

    String forgotPassword(String email);

    //dùng cho reset-token
//    String resetPassword(String secretKey);

    String changePassword(@Valid ResetPasswordRequest request);
}
