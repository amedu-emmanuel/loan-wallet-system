package com.koins.loanwallet.repository;

import com.koins.loanwallet.entity.Loan;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByUser(User user);

    List<Loan> findByUserId(UUID userId);

    List<Loan> findByLoanStatus(LoanStatus loanStatus);

    List<Loan> findByUserIdAndLoanStatus(UUID userId, LoanStatus loanStatus);
}