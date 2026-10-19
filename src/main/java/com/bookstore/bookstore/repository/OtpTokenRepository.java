package com.bookstore.bookstore.repository;

import com.bookstore.bookstore.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(String email, String type);
    void deleteByExpiresAtBefore(LocalDateTime time);
}
