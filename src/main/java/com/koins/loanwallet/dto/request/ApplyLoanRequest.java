package com.koins.loanwallet.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyLoanRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "0.01", message = "Loan amount must be greater than zero")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan duration is required")
    @Positive(message = "Loan duration must be greater than zero")
    private Integer loanDurationDays;
}