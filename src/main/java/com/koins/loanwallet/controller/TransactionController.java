package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.dto.response.TransactionResponse;
import com.koins.loanwallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;

@Tag(name = "Transactions", description = "Transaction retrieval APIs")

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Get all transactions for current user")
    @GetMapping
    public ApiResponse<List<TransactionResponse>> getMyTransactions() {
        return ApiResponse.<List<TransactionResponse>>builder()
                .success(true)
                .message("Transactions retrieved successfully")
                .data(transactionService.getMyTransactions())
                .build();
    }

    @Operation(summary = "Get a single transaction by ID")
    @GetMapping("/{transactionId}")
    public ApiResponse<TransactionResponse> getTransactionById(@PathVariable UUID transactionId) {
        return ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Transaction retrieved successfully")
                .data(transactionService.getMyTransactionById(transactionId))
                .build();
    }
}