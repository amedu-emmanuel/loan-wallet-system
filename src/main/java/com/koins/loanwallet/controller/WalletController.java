package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.request.FundWalletRequest;
import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.dto.response.TransactionResponse;
import com.koins.loanwallet.dto.response.WalletResponse;
import com.koins.loanwallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Wallet", description = "Wallet balance, wallet funding, and wallet transaction APIs")

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @Operation(summary = "Get current user's wallet")
    @GetMapping
    public ApiResponse<WalletResponse> getMyWallet() {
        return ApiResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet retrieved successfully")
                .data(walletService.getMyWallet())
                .build();
    }

    @Operation(summary = "Initiate wallet funding")
    @PostMapping("/fund")
    public ApiResponse<TransactionResponse> fundWallet(@Valid @RequestBody FundWalletRequest request) {
        return ApiResponse.<TransactionResponse>builder()
                .success(true)
                .message("Wallet funding initiated successfully")
                .data(walletService.fundWallet(request))
                .build();
    }

    @Operation(summary = "Get current user's wallet transactions")
    @GetMapping("/transactions")
    public ApiResponse<List<TransactionResponse>> getMyTransactions() {
        return ApiResponse.<List<TransactionResponse>>builder()
                .success(true)
                .message("Transactions retrieved successfully")
                .data(walletService.getMyTransactions())
                .build();
    }
}