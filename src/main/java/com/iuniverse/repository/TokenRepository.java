package com.iuniverse.repository;

import com.iuniverse.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByUsername(String username);

    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByResetToken(String resetToken);

    Optional<Token> findByUsernameAndDeviceInfo(String username, String deviceInfo);

    @Modifying
    @Transactional
    @Query("DELETE FROM Token t WHERE t.username = :username AND t.resetToken IS NOT NULL")
    void deleteResetTokensByUsername(String username);

    Optional<Token> findByUsernameAndResetToken(String username, String resetToken);
}
