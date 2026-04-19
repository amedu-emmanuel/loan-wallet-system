package com.koins.loanwallet.service;

import com.koins.loanwallet.dto.response.TransactionResponse;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    List<TransactionResponse> getMyTransactions();

    TransactionResponse getMyTransactionById(UUID transactionId);
}