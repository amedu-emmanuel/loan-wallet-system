package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.PaymentWebhookRequest;
import com.koins.loanwallet.entity.Transaction;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import com.koins.loanwallet.enums.TransactionStatus;
import com.koins.loanwallet.enums.TransactionType;
import com.koins.loanwallet.enums.WalletStatus;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.repository.TransactionRepository;
import com.koins.loanwallet.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processPaymentWebhook_shouldCreditWalletAndMarkTransactionSuccessful() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("emmanuel@test.com")
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balance(new BigDecimal("5000.00"))
                .currency("NGN")
                .status(WalletStatus.ACTIVE)
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .wallet(wallet)
                .transactionType(TransactionType.WALLET_FUNDING)
                .amount(new BigDecimal("3000.00"))
                .transactionStatus(TransactionStatus.PENDING)
                .referenceNumber("FUND-123456")
                .timestamp(LocalDateTime.now())
                .build();

        PaymentWebhookRequest request = new PaymentWebhookRequest();
        request.setReference("FUND-123456");
        request.setStatus("success");
        request.setAmount(new BigDecimal("3000"));

        when(transactionRepository.findByReferenceNumber("FUND-123456")).thenReturn(Optional.of(transaction));

        paymentService.processPaymentWebhook(request);

        assertEquals(new BigDecimal("8000.00"), wallet.getBalance());
        assertEquals(TransactionStatus.SUCCESS, transaction.getTransactionStatus());

        verify(walletRepository).save(wallet);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void processPaymentWebhook_shouldThrowExceptionWhenAmountDoesNotMatch() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balance(new BigDecimal("5000.00"))
                .build();

        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .wallet(wallet)
                .amount(new BigDecimal("3000.00"))
                .transactionStatus(TransactionStatus.PENDING)
                .referenceNumber("FUND-123456")
                .build();

        PaymentWebhookRequest request = new PaymentWebhookRequest();
        request.setReference("FUND-123456");
        request.setStatus("success");
        request.setAmount(new BigDecimal("4000"));

        when(transactionRepository.findByReferenceNumber("FUND-123456")).thenReturn(Optional.of(transaction));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> paymentService.processPaymentWebhook(request)
        );

        assertEquals("Webhook amount does not match transaction amount", exception.getMessage());
    }
}