package com.koins.loanwallet.service.impl;

import com.koins.loanwallet.dto.request.PaymentWebhookRequest;
import com.koins.loanwallet.entity.Transaction;
import com.koins.loanwallet.entity.Wallet;
import com.koins.loanwallet.enums.TransactionStatus;
import com.koins.loanwallet.exception.BadRequestException;
import com.koins.loanwallet.exception.ResourceNotFoundException;
import com.koins.loanwallet.repository.TransactionRepository;
import com.koins.loanwallet.repository.WalletRepository;
import com.koins.loanwallet.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void processPaymentWebhook(PaymentWebhookRequest request) {
        Transaction transaction = transactionRepository.findByReferenceNumber(request.getReference())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for reference"));

        if (transaction.getTransactionStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException("Transaction has already been processed");
        }

        if (transaction.getAmount().compareTo(request.getAmount()) != 0) {
            throw new BadRequestException("Webhook amount does not match transaction amount");
        }

        if ("success".equalsIgnoreCase(request.getStatus())) {
            Wallet wallet = transaction.getWallet();
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
            walletRepository.save(wallet);

            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);
        } else {
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
        }
    }
}