package com.iuniverse.service;

import com.iuniverse.common.TokenType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public interface JwtService {
    String generateAccessToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities);

    String generateRefreshToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities);

    String generateResetToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities);


    String extractUsername(String token, TokenType tokenType);

    Long extractUserId(String token, TokenType tokenType);

    boolean isTokenValid(String token, TokenType tokenType, UserDetails userDetails);
}
