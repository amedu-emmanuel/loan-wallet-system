package com.koins.loanwallet.dto.response;

import com.koins.loanwallet.enums.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal loanAmount;
    private BigDecimal interestRate;
    private Integer loanDurationDays;
    private LoanStatus loanStatus;
    private String repaymentSchedule;
    private BigDecimal totalRepayableAmount;
    private BigDecimal amountRepaid;
    private LocalDate dueDate;
    private LocalDateTime disbursedAt;
    private LocalDateTime repaidAt;
    private LocalDateTime createdAt;
}