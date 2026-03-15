package com.iuniverse.controller;

import com.iuniverse.controller.request.LogoutRequest;
import com.iuniverse.controller.request.RefreshTokenRequest;
import com.iuniverse.controller.request.ResetPasswordRequest;
import com.iuniverse.controller.request.SignInRequest;
import com.iuniverse.controller.response.TokenResponse;
import com.iuniverse.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@Tag(name = "Authentication Controller", description = "APIs for authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Get access token", description = "Get access & refresh token by username, password")
    @PostMapping("/access-token")
    public ResponseEntity<TokenResponse>  getAccessToken(@RequestBody @Valid SignInRequest signInRequest) {
        log.info("Access token request");
        return new ResponseEntity<>(authenticationService.getAccessToken(signInRequest), OK) ;
    }

    @Operation(summary = "Get refresh token", description = "Get new access token by refresh token ")
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> getRefreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        log.info("Refresh token request");
        return new ResponseEntity<>(authenticationService.getRefreshToken(refreshTokenRequest), OK);
    }

    @Operation(summary = "Logout", description = "Logout and blacklist token")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @RequestBody @Valid LogoutRequest logoutRequest) {
        log.info("Logout request");

        return new ResponseEntity<>(authenticationService.logout(request, logoutRequest), OK);
    }

    @Operation(summary = "Forgot password", description = "Send email to user to reset password")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        return new ResponseEntity<>(authenticationService.forgotPassword(email), OK);
    }

    @Operation(summary = "Reset password", description = "Reset password by email")
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody String secretKey) {
        return new ResponseEntity<>(authenticationService.resetPassword(secretKey), OK);
    }

    @Operation(summary = "Change password", description = "Change password by old password and new password")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ResetPasswordRequest request) {
        return new ResponseEntity<>(authenticationService.changePassword(request), OK);
    }
}