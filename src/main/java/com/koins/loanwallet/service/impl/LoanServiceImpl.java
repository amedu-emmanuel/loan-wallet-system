package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.ApplyLoanRequest;
import com.koins.loanwallet.dto.request.RepayLoanRequest;
import com.koins.loanwallet.dto.response.LoanResponse;
import com.koins.loanwallet.entity.Loan;
import com.koins.loanwallet.entity.Transaction;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import com.koins.loanwallet.enums.LoanStatus;
import com.koins.loanwallet.enums.TransactionStatus;
import com.koins.loanwallet.enums.TransactionType;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.exception.ResourceNotFoundException;
import com.koins.loanwallet.repository.LoanRepository;
import com.koins.loanwallet.repository.TransactionRepository;
import com.koins.loanwallet.repository.UserRepository;
import com.koins.loanwallet.repository.WalletRepository;
import com.koins.loanwallet.security.SecurityUtils;
import com.koins.loanwallet.service.LoanService;
import com.koins.loanwallet.util.AppConstants;
import com.koins.loanwallet.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public LoanResponse applyForLoan(ApplyLoanRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Only users with funded wallets can apply for loans");
        }

        BigDecimal maxLoanAmount = wallet.getBalance().multiply(BigDecimal.valueOf(3));
        if (request.getLoanAmount().compareTo(maxLoanAmount) > 0) {
            throw new BadRequestException("Loan amount must not exceed 3 times wallet balance");
        }

        BigDecimal interestRate = AppConstants.DEFAULT_INTEREST_RATE;
        BigDecimal interestAmount = request.getLoanAmount()
                .multiply(interestRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal totalRepayable = request.getLoanAmount().add(interestAmount);
        LocalDate dueDate = LocalDate.now().plusDays(request.getLoanDurationDays());

        String repaymentSchedule = String.format(
                "{\"principal\": %s, \"interestRate\": %s, \"totalRepayable\": %s, \"durationDays\": %d, \"dueDate\": \"%s\"}",
                request.getLoanAmount(),
                interestRate,
                totalRepayable,
                request.getLoanDurationDays(),
                dueDate
        );

        Loan loan = Loan.builder()
                .user(user)
                .loanAmount(request.getLoanAmount())
                .interestRate(interestRate)
                .loanDurationDays(request.getLoanDurationDays())
                .loanStatus(LoanStatus.PENDING)
                .repaymentSchedule(repaymentSchedule)
                .totalRepayableAmount(totalRepayable)
                .amountRepaid(BigDecimal.ZERO)
                .dueDate(dueDate)
                .build();

        loan.setCreatedAt(LocalDateTime.now());

        Loan savedLoan = loanRepository.save(loan);

        return mapToLoanResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse approveLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getLoanStatus() != LoanStatus.PENDING) {
            throw new BadRequestException("Only pending loans can be approved");
        }

        loan.setLoanStatus(LoanStatus.APPROVED);

        Loan updatedLoan = loanRepository.save(loan);
        return mapToLoanResponse(updatedLoan);
    }

    @Override
    @Transactional
    public LoanResponse disburseLoan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getLoanStatus() != LoanStatus.APPROVED) {
            throw new BadRequestException("Only approved loans can be disbursed");
        }

        Wallet wallet = walletRepository.findByUserId(loan.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(loan.getLoanAmount()));
        walletRepository.save(wallet);

        loan.setLoanStatus(LoanStatus.DISBURSED);
        loan.setDisbursedAt(LocalDateTime.now());

        Loan updatedLoan = loanRepository.save(loan);

        Transaction transaction = Transaction.builder()
                .user(loan.getUser())
                .wallet(wallet)
                .transactionType(TransactionType.LOAN_DISBURSEMENT)
                .amount(loan.getLoanAmount())
                .transactionStatus(TransactionStatus.SUCCESS)
                .referenceNumber(ReferenceGenerator.generateReference("LOAN"))
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return mapToLoanResponse(updatedLoan);
    }

    @Override
    @Transactional
    public LoanResponse repayLoan(UUID loanId, RepayLoanRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only repay your own loan");
        }

        if (loan.getLoanStatus() != LoanStatus.DISBURSED) {
            throw new BadRequestException("Only disbursed loans can be repaid");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient wallet balance for repayment");
        }

        BigDecimal remainingAmount = loan.getTotalRepayableAmount().subtract(loan.getAmountRepaid());
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BadRequestException("Repayment amount exceeds outstanding loan balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(wallet);

        loan.setAmountRepaid(loan.getAmountRepaid().add(request.getAmount()));

        if (loan.getAmountRepaid().compareTo(loan.getTotalRepayableAmount()) >= 0) {
            loan.setLoanStatus(LoanStatus.REPAID);
            loan.setRepaidAt(LocalDateTime.now());
        }

        Loan updatedLoan = loanRepository.save(loan);

        Transaction transaction = Transaction.builder()
                .user(loan.getUser())
                .wallet(wallet)
                .transactionType(TransactionType.REPAYMENT)
                .amount(request.getAmount())
                .transactionStatus(TransactionStatus.SUCCESS)
                .referenceNumber(ReferenceGenerator.generateReference("REPAY"))
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return mapToLoanResponse(updatedLoan);
    }

    @Override
    public LoanResponse getLoanById(UUID loanId) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only view your own loan");
        }

        return mapToLoanResponse(loan);
    }

    @Override
    public List<LoanResponse> getMyLoans() {
        UUID userId = SecurityUtils.getCurrentUserId();

        return loanRepository.findByUserId(userId)
                .stream()
                .map(this::mapToLoanResponse)
                .toList();
    }

    private LoanResponse mapToLoanResponse(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .userId(loan.getUser().getId())
                .loanAmount(loan.getLoanAmount())
                .interestRate(loan.getInterestRate())
                .loanDurationDays(loan.getLoanDurationDays())
                .loanStatus(loan.getLoanStatus())
                .repaymentSchedule(loan.getRepaymentSchedule())
                .totalRepayableAmount(loan.getTotalRepayableAmount())
                .amountRepaid(loan.getAmountRepaid())
                .dueDate(loan.getDueDate())
                .disbursedAt(loan.getDisbursedAt())
                .repaidAt(loan.getRepaidAt())
                .createdAt(loan.getCreatedAt())
                .build();
    }
}