package com.iuniverse.service.impl;

import com.iuniverse.controller.request.LogoutRequest;
import com.iuniverse.controller.request.RefreshTokenRequest;
import com.iuniverse.controller.request.ResetPasswordRequest;
import com.iuniverse.controller.request.SignInRequest;
import com.iuniverse.controller.response.TokenResponse;
import com.iuniverse.exception.InvalidDataException;
import com.iuniverse.model.Token;
import com.iuniverse.model.User;
import com.iuniverse.repository.UserRepository;
import com.iuniverse.service.*;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

import static com.iuniverse.common.TokenType.REFRESH_TOKEN;
import static com.iuniverse.common.TokenType.RESET_TOKEN;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "AUTHENTICATION-SERVICE")
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public TokenResponse getAccessToken(SignInRequest request) {
        log.info("Get access token for user {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (AuthenticationException e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new AccessDeniedException(e.getMessage());
        }

        var user = userRepository.findByUsername(request.getUsername());
        
        if(user == null) {
            log.error("User not found with username: {}", request.getUsername());
            throw new UsernameNotFoundException("User not found");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), request.getUsername(), user.getAuthorities());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), request.getUsername(), user.getAuthorities());

        String deviceInfo = request.getPlatform();

        Token existingToken = tokenService.getTokenByUsernameAndDeviceInfo(user.getUsername(), deviceInfo);

        if (existingToken != null) {
            // Thiết bị này đã đăng nhập trước đó -> ghi đè token mới
            existingToken.setAccessToken(accessToken);
            existingToken.setRefreshToken(refreshToken);
            tokenService.save(existingToken);
            log.info("Updated existing tokens for device: {}", deviceInfo);
        } else {
            // Thiết bị mới tinh -> TẠO MỚI
            tokenService.save(Token.builder()
                    .username(user.getUsername())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .deviceInfo(deviceInfo)
                    .build());
            log.info("Created new tokens for new device: {}", deviceInfo);
        }

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public TokenResponse getRefreshToken(RefreshTokenRequest request) {
        log.info("Get new access token using refresh token");

        String refreshToken = request.getRefreshToken();
        if (StringUtils.isBlank(refreshToken)) {
            throw new InvalidDataException("Token must be not blank");
        }
        final String userName = jwtService.extractUsername(refreshToken, REFRESH_TOKEN);
        var user = userService.findByUsername(userName);

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, REFRESH_TOKEN, (UserDetails) user)) {
            log.error("Invalid or expired refresh token");
            throw new AccessDeniedException("Invalid or expired refresh token");
        }

        // Extract username and userId from refresh token
        String username = jwtService.extractUsername(refreshToken, REFRESH_TOKEN);
        log.info("Generating new access token for user {}", username);

        // Verify user still exists
        if (user == null) {
            log.error("User not found with username: {}", username);
            throw new UsernameNotFoundException("User not found");
        }

        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user.getId(), username, null);

        //save token to db
        Token currentToken = tokenService.getTokenByRefreshToken(refreshToken);

        if (currentToken != null) {
            currentToken.setAccessToken(newAccessToken);
            tokenService.save(currentToken);
            log.info("Successfully updated new access token in database");
        }

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return the same refresh token
                .build();
    }

    @Override
    public String logout(HttpServletRequest request, LogoutRequest logoutRequest) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid token provided in the request");
            throw new InvalidDataException("No valid token provided");
        }

        String deviceRefreshToken = logoutRequest.getRefreshToken();
        log.info("Processing logout for device refresh token...");

        Token currentToken = tokenService.getTokenByRefreshToken(deviceRefreshToken);

        if (currentToken != null) {
            tokenService.delete(currentToken);
            log.info("Successfully deleted token for this specific device");
        } else {
            log.info("Token already deleted or not found");
        }

        return "Logout successfully!";
    }

    @Override
    public String forgotPassword(String email) {
        log.info("Processing forgot password request for email: {}", email);

        User user = userService.getUserByEmail(email);

        if(!user.isEnabled()) {
            throw new InvalidDataException("User is not active");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));

        tokenService.deleteResetTokensByUsername(user.getUsername());
        log.info("Cleared old OTPs for user: {}", user.getUsername());

        tokenService.save(Token.builder()
                .username(user.getUsername())
                .resetToken(otp)
                .build());

        emailService.sendResetPasswordEmail(user.getEmail(), otp);

        return "OTP has been sent to your email!";
    }

    @Override
    public String changePassword(ResetPasswordRequest request) {
        log.info("----- verify OTP and change password -----");

        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidDataException("Password and confirm password do not match");
        }

        User user = userService.getUserByEmail(request.getEmail());
        if(!user.isEnabled()) {
            throw new InvalidDataException("User is not active");
        }

        // Verify OTP in the database
        Token tokenInDb = tokenService.getTokenByUsernameAndOtp(user.getUsername(), request.getOtp());
        if (tokenInDb == null) {
            throw new InvalidDataException("OTP is invalid or has expired!");
        }


        long timeDiff = System.currentTimeMillis() - tokenInDb.getCreatedAt().getTime();
        if (timeDiff > 5 * 60 * 1000) {
            throw new InvalidDataException("OTP has expired");
        }


        // Everything is valid -> Change password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userService.saveUser(user);

        // Delete the OTP immediately so it cannot be reused
        tokenService.delete(tokenInDb);
        log.info("Deleted OTP token for user: {}", user.getUsername());

        return "Password changed successfully!";
    }

    //dùng cho reset-token
//    @Override
//    public String resetPassword(String secretKey) {
//        log.info("----- reset password -----");
//
//        final String userName = jwtService.extractUsername(secretKey, RESET_TOKEN);
//
//        var user = userRepository.findByUsername(userName);
//
//        if(!jwtService.isTokenValid(secretKey, RESET_TOKEN, user)) {
//            throw new AccessDeniedException("Invalid or expired refresh token");
//        }
//
//        return "Reset";
//    }

}

