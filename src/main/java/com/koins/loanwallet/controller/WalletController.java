package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.request.FundWalletRequest;
import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.dto.response.TransactionResponse;
import com.koins.loanwallet.dto.response.WalletResponse;
import com.koins.loanwallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ApiResponse<WalletResponse> getMyWallet() {
        return ApiResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet retrieved successfully")
                .data(walletService.getMyWallet())
                .build();
    }

    @PostMapping("/fund")
    public ApiResponse<WalletResponse> fundWallet(@Valid @RequestBody FundWalletRequest request) {
        return ApiResponse.<WalletResponse>builder()
                .success(true)
                .message("Wallet funded successfully")
                .data(walletService.fundWallet(request))
                .build();
    }

    @GetMapping("/transactions")
    public ApiResponse<List<TransactionResponse>> getMyTransactions() {
        return ApiResponse.<List<TransactionResponse>>builder()
                .success(true)
                .message("Transactions retrieved successfully")
                .data(walletService.getMyTransactions())
                .build();
    }
}