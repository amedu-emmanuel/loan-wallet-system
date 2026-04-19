package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.FundWalletRequest;
import com.koins.loanwallet.dto.response.TransactionResponse;
import com.koins.loanwallet.dto.response.WalletResponse;
import com.koins.loanwallet.entity.Transaction;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import com.koins.loanwallet.enums.TransactionStatus;
import com.koins.loanwallet.enums.TransactionType;
import com.koins.loanwallet.enums.WalletStatus;
import com.koins.loanwallet.repository.TransactionRepository;
import com.koins.loanwallet.repository.UserRepository;
import com.koins.loanwallet.repository.WalletRepository;
import com.koins.loanwallet.security.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

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
        wallet.setCreatedAt(LocalDateTime.now());

        CustomUserPrincipal principal = mock(CustomUserPrincipal.class);
        when(principal.getId()).thenReturn(userId);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getMyWallet_shouldReturnWalletSuccessfully() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.getMyWallet();

        assertNotNull(response);
        assertEquals(wallet.getId(), response.getId());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
    }

    @Test
    void fundWallet_shouldCreatePendingTransaction() {
        FundWalletRequest request = new FundWalletRequest();
        request.setAmount(new BigDecimal("3000"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(UUID.randomUUID());
            return tx;
        });

        TransactionResponse response = walletService.fundWallet(request);

        assertNotNull(response);
        assertEquals(TransactionType.WALLET_FUNDING, response.getTransactionType());
        assertEquals(TransactionStatus.PENDING, response.getTransactionStatus());
        assertEquals(new BigDecimal("3000"), response.getAmount());

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getMyTransactions_shouldReturnUserTransactions() {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .wallet(wallet)
                .transactionType(TransactionType.WALLET_FUNDING)
                .amount(new BigDecimal("3000"))
                .transactionStatus(TransactionStatus.SUCCESS)
                .referenceNumber("FUND-123456")
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionRepository.findByUserIdOrderByTimestampDesc(userId))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> responses = walletService.getMyTransactions();

        assertEquals(1, responses.size());
        assertEquals("FUND-123456", responses.get(0).getReferenceNumber());
    }
}