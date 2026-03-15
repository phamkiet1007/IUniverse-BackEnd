package com.iuniverse.service;

import com.iuniverse.model.Token;
import com.iuniverse.repository.TokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public record TokenService(TokenRepository tokenRepository) {

    public int save(Token token) {
        Token savedToken = tokenRepository.save(token);
        return savedToken.getId();
    }

    public String delete(Token token) {
        tokenRepository.delete(token);
        return "Deleted token for user: " + token.getUsername();
    }


    //hàm này ko dùng nữa vì giờ 1 username sẽ có nhiều tokens
    public Token getTokenByUsername(String username) {
        return tokenRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Token not found for username: " + username));
    }

    public Token getTokenByRefreshToken(String refreshToken) {
        return tokenRepository.findByRefreshToken(refreshToken)
                .orElse(null);
    }

    //dùng cho reset-token
//    public Token getTokenByResetToken(String resetToken) {
//        return tokenRepository.findByResetToken(resetToken).orElse(null);
//    }

    public Token getTokenByUsernameAndOtp(String username, String otp) {
        return tokenRepository.findByUsernameAndResetToken(username, otp).orElse(null);
    }

    public Token getTokenByUsernameAndDeviceInfo(String username, String deviceInfo) {
        return tokenRepository.findByUsernameAndDeviceInfo(username, deviceInfo).orElse(null);
    }

    public void deleteResetTokensByUsername(String username) {
        tokenRepository.deleteResetTokensByUsername(username);
    }
}
