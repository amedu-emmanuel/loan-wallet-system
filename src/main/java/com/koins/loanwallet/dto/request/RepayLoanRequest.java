package com.koins.loanwallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepayLoanRequest {

    @NotNull(message = "Repayment amount is required")
    @DecimalMin(value = "0.01", message = "Repayment amount must be greater than zero")
    private BigDecimal amount;
}