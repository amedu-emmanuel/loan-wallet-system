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
import com.koins.loanwallet.enums.WalletStatus;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.repository.LoanRepository;
import com.koins.loanwallet.repository.TransactionRepository;
import com.koins.loanwallet.repository.UserRepository;
import com.koins.loanwallet.repository.WalletRepository;
import com.koins.loanwallet.security.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private UUID userId;
    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .email("emmanuel@test.com")
                .build();

        wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balance(new BigDecimal("5000.00"))
                .currency("NGN")
                .status(WalletStatus.ACTIVE)
                .build();

        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        lenient().when(principal.getId()).thenReturn(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void applyForLoan_shouldSucceedWhenWalletIsFunded() {
        ApplyLoanRequest request = new ApplyLoanRequest();
        request.setLoanAmount(new BigDecimal("10000"));
        request.setLoanDurationDays(30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(UUID.randomUUID());
            return loan;
        });

        LoanResponse response = loanService.applyForLoan(request);

        assertNotNull(response);
        assertEquals(LoanStatus.PENDING, response.getLoanStatus());
        assertEquals(new BigDecimal("10000"), response.getLoanAmount());
    }

    @Test
    void applyForLoan_shouldFailWhenAmountExceedsThreeTimesWalletBalance() {
        ApplyLoanRequest request = new ApplyLoanRequest();
        request.setLoanAmount(new BigDecimal("20000"));
        request.setLoanDurationDays(30);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loanService.applyForLoan(request)
        );

        assertEquals("Loan amount must not exceed 3 times wallet balance", exception.getMessage());
    }

    @Test
    void approveLoan_shouldUpdateStatusToApproved() {
        Loan loan = Loan.builder()
                .id(UUID.randomUUID())
                .user(user)
                .loanAmount(new BigDecimal("10000"))
                .loanStatus(LoanStatus.PENDING)
                .build();

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.approveLoan(loan.getId());

        assertEquals(LoanStatus.APPROVED, response.getLoanStatus());
    }

    @Test
    void repayLoan_shouldFailWhenWalletBalanceIsInsufficient() {
        Loan loan = Loan.builder()
                .id(UUID.randomUUID())
                .user(user)
                .loanAmount(new BigDecimal("10000"))
                .loanStatus(LoanStatus.DISBURSED)
                .totalRepayableAmount(new BigDecimal("10500"))
                .amountRepaid(BigDecimal.ZERO)
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        wallet.setBalance(new BigDecimal("1000"));

        RepayLoanRequest request = new RepayLoanRequest();
        request.setAmount(new BigDecimal("2000"));

        when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> loanService.repayLoan(loan.getId(), request)
        );

        assertEquals("Insufficient wallet balance for repayment", exception.getMessage());
    }
}