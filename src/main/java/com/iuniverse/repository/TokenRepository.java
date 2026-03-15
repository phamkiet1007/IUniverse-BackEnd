package com.iuniverse.repository;

import com.iuniverse.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByUsername(String username);

    Optional<Token> findByRefreshToken(String refreshToken);
}
