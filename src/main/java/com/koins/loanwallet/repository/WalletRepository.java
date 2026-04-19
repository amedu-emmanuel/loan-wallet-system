package com.koins.loanwallet.repository;

import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserId(UUID userId);

    boolean existsByUser(User user);
}