package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.response.TransactionResponse;
import com.koins.loanwallet.entity.Transaction;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.exception.ResourceNotFoundException;
import com.koins.loanwallet.repository.TransactionRepository;
import com.koins.loanwallet.security.SecurityUtils;
import com.koins.loanwallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<TransactionResponse> getMyTransactions() {
        UUID userId = SecurityUtils.getCurrentUserId();

        return transactionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    @Override
    public TransactionResponse getMyTransactionById(UUID transactionId) {
        UUID userId = SecurityUtils.getCurrentUserId();

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only view your own transaction");
        }

        return mapToTransactionResponse(transaction);
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