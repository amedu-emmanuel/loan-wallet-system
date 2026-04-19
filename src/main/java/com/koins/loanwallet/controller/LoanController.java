package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.request.ApplyLoanRequest;
import com.koins.loanwallet.dto.request.RepayLoanRequest;
import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.dto.response.LoanResponse;
import com.koins.loanwallet.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ApiResponse<LoanResponse> applyForLoan(@Valid @RequestBody ApplyLoanRequest request) {
        return ApiResponse.<LoanResponse>builder()
                .success(true)
                .message("Loan application submitted successfully")
                .data(loanService.applyForLoan(request))
                .build();
    }

    @PostMapping("/{loanId}/approve")
    public ApiResponse<LoanResponse> approveLoan(@PathVariable UUID loanId) {
        return ApiResponse.<LoanResponse>builder()
                .success(true)
                .message("Loan approved successfully")
                .data(loanService.approveLoan(loanId))
                .build();
    }

    @PostMapping("/{loanId}/disburse")
    public ApiResponse<LoanResponse> disburseLoan(@PathVariable UUID loanId) {
        return ApiResponse.<LoanResponse>builder()
                .success(true)
                .message("Loan disbursed successfully")
                .data(loanService.disburseLoan(loanId))
                .build();
    }

    @PostMapping("/{loanId}/repay")
    public ApiResponse<LoanResponse> repayLoan(@PathVariable UUID loanId,
                                               @Valid @RequestBody RepayLoanRequest request) {
        return ApiResponse.<LoanResponse>builder()
                .success(true)
                .message("Loan repayment successful")
                .data(loanService.repayLoan(loanId, request))
                .build();
    }

    @GetMapping("/{loanId}")
    public ApiResponse<LoanResponse> getLoanById(@PathVariable UUID loanId) {
        return ApiResponse.<LoanResponse>builder()
                .success(true)
                .message("Loan retrieved successfully")
                .data(loanService.getLoanById(loanId))
                .build();
    }

    @GetMapping
    public ApiResponse<List<LoanResponse>> getMyLoans() {
        return ApiResponse.<List<LoanResponse>>builder()
                .success(true)
                .message("Loans retrieved successfully")
                .data(loanService.getMyLoans())
                .build();
    }
}