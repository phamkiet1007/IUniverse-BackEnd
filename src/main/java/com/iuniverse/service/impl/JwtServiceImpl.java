package com.iuniverse.service.impl;

import com.iuniverse.common.TokenType;
import com.iuniverse.exception.InvalidDataException;
import com.iuniverse.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.iuniverse.common.TokenType.*;

@Service
@Slf4j(topic = "JWT-SERVICE")
public class JwtServiceImpl implements JwtService {

    private final View error;
    @Value("${jwt.expiryTimeInMinutes}")
    private int expiryTimeInMinutes;

    @Value("${jwt.expiryTimeInDays}")
    private int expiryTimeInDays;

    @Value("${jwt.accessKey}")
    private String accessKey;

    @Value("${jwt.refreshKey}")
    private String refreshKey;

    @Value("${jwt.resetKey}")
    private String resetKey;

    public JwtServiceImpl(View error) {
        this.error = error;
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        log.info("Extract username from token {} with type {}", token, tokenType);


        return extractClaims(tokenType, token, Claims::getSubject);
    }

    @Override
    public Long extractUserId(String token, TokenType tokenType) {
        log.info("Extract userId from token");
        // Lấy claim "userId" ra. Dùng Number.class để tránh lỗi ép kiểu giữa Integer và Long của thư viện Jackson
        Number userId = extractClaims(tokenType, token, claims -> claims.get("userId", Number.class));
        return userId != null ? userId.longValue() : null;
    }

    @Override
    public boolean isTokenValid(String token, TokenType type, UserDetails userDetails) {
        final String username = extractUsername(token, type);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, type));
    }

    private boolean isTokenExpired(String token, TokenType type) {
        return extractExpiration(type, token).before(new Date());
    }

    private Date extractExpiration(TokenType type, String token) {
        return extractClaims(type, token, Claims::getExpiration);
    }

    private <T> T extractClaims(TokenType tokenType, String token, Function<Claims, T> claimsExtractor) {
        final Claims claims = extractAllClaims(token, tokenType);

        return claimsExtractor.apply(claims);
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        try {
            return Jwts.parser()
                    .setSigningKey(getPrivateKey(tokenType))
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException | ExpiredJwtException e) {
            log.error("Error extracting claims from token {} with type {}: {}", token, tokenType, e.getMessage());
            throw new AccessDeniedException("Access denied!, error: " + e.getMessage());
        }
    }

    @Override
    public String generateAccessToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate access token for user {} with authorities {}", username, authorities);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);


        return generateAccessToken(claims, username);
    }

    @Override
    public String generateRefreshToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate refresh token for user {} with authorities {}", username, authorities);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);


        return generateRefreshToken(claims, username);
    }

    @Override
    public String generateResetToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities) {
        log.info("Generate reset token for user {} with authorities {}", username, authorities);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", authorities);


        return generateResetToken(claims, username);
    }


    private String generateAccessToken(Map<String, Object> claims, String username) {
        log.info("Generate access token for user {} with name {}", username, claims);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * expiryTimeInMinutes))
                .signWith(getPrivateKey(ACCESS_TOKEN), SignatureAlgorithm.HS256)
                .compact();

    }

    private String generateRefreshToken(Map<String, Object> claims, String username) {
        log.info("Generate refresh token for user {} with name {}", username, claims);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * expiryTimeInDays))
                .signWith(getPrivateKey(REFRESH_TOKEN), SignatureAlgorithm.HS256)
                .compact();

    }

    private String generateResetToken(Map<String, Object> claims, String username) {
        log.info("Generate refresh token for user {} with name {}", username, claims);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60))
                .signWith(getPrivateKey(RESET_TOKEN), SignatureAlgorithm.HS256)
                .compact();

    }

    private Key getPrivateKey(TokenType tokenType) {
        switch (tokenType) {
            case ACCESS_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
            }
            case REFRESH_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshKey));
            }
            case RESET_TOKEN -> {
                return Keys.hmacShaKeyFor(Decoders.BASE64.decode(resetKey));
            }
            default -> throw new InvalidDataException("Invalid token type");
        }
    }


}
