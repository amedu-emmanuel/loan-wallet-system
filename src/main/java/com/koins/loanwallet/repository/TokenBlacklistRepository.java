package com.koins.loanwallet.repository;

import com.koins.loanwallet.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, UUID> {

    boolean existsByToken(String token);

    void deleteByExpiresAtBefore(LocalDateTime time);
}