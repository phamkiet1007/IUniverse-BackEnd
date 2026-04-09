package com.iuniverse.controller;

import com.iuniverse.controller.request.*;
import com.iuniverse.controller.response.TokenResponse;
import com.iuniverse.service.AuthenticationService;
import com.iuniverse.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-CONTROLLER")
@Tag(name = "Authentication Controller", description = "APIs for authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

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
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return new ResponseEntity<>(authenticationService.forgotPassword(email), OK);
    }

    //dùng cho reset-token
//    @Operation(summary = "Reset password", description = "Reset password by email")
//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestBody String secretKey) {
//        return new ResponseEntity<>(authenticationService.resetPassword(secretKey), OK);
//    }

    @Operation(summary = "Change password", description = "Change password by old password and new password")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ResetPasswordRequest request) {
        return new ResponseEntity<>(authenticationService.changePassword(request), OK);
    }

    @Operation(summary = "Verify OTP", description = "Active account by OTP")
    @PostMapping("/verify-account")
    public ResponseEntity<Object> verifyAccount(@RequestBody @Valid VerifyOtpRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());

        userService.verifyOtp(request.getEmail(), request.getOtp());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "Account activated successfully!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Resend OTP", description = "Resend OTP if the old one is expired")
    @PostMapping("/resend-otp")
    public ResponseEntity<Object> resendOtp(@RequestParam String email) {
        log.info("Receive request resend OTP cho email: {}", email);

        userService.resendOtp(email);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "A new OTP has been sent to your email!");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}