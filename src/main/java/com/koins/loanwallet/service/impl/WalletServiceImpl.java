package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.FundWalletRequest;
import com.koins.loanwallet.dto.response.TransactionResponse;
import com.koins.loanwallet.dto.response.WalletResponse;
import com.koins.loanwallet.entity.Transaction;
import com.koins.loanwallet.entity.User;
import com.koins.loanwallet.entity.Wallet;
import com.koins.loanwallet.enums.TransactionStatus;
import com.koins.loanwallet.enums.TransactionType;
import com.koins.loanwallet.exception.ResourceNotFoundException;
import com.koins.loanwallet.repository.TransactionRepository;
import com.koins.loanwallet.repository.UserRepository;
import com.koins.loanwallet.repository.WalletRepository;
import com.koins.loanwallet.security.SecurityUtils;
import com.koins.loanwallet.service.WalletService;
import com.koins.loanwallet.util.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public WalletResponse getMyWallet() {
        UUID userId = SecurityUtils.getCurrentUserId();

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse fundWallet(FundWalletRequest request) {
        UUID userId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Wallet updatedWallet = walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .user(user)
                .wallet(updatedWallet)
                .transactionType(TransactionType.WALLET_FUNDING)
                .amount(request.getAmount())
                .transactionStatus(TransactionStatus.SUCCESS)
                .referenceNumber(ReferenceGenerator.generateReference("FUND"))
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return mapToWalletResponse(updatedWallet);
    }

    @Override
    public List<TransactionResponse> getMyTransactions() {
        UUID userId = SecurityUtils.getCurrentUserId();

        return transactionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUser().getId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .walletId(transaction.getWallet().getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .transactionStatus(transaction.getTransactionStatus())
                .referenceNumber(transaction.getReferenceNumber())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}