package com.koins.loanwallet.repository;

import com.koins.loanwallet.entity.Transaction;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByUserOrderByTimestampDesc(User user);

    List<Transaction> findByUserIdOrderByTimestampDesc(UUID userId);

    List<Transaction> findByWalletOrderByTimestampDesc(Wallet wallet);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);
}