package com.koins.loanwallet.repository;

import com.koins.loanwallet.entity.Otp;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OtpRepository extends JpaRepository<Otp, UUID> {

    Optional<Otp> findTopByUserAndPurposeAndUsedFalseOrderByCreatedAtDesc(User user, OtpPurpose purpose);

    List<Otp> findByUserAndPurposeAndUsedFalse(User user, OtpPurpose purpose);

    void deleteByExpiresAtBefore(LocalDateTime time);
}