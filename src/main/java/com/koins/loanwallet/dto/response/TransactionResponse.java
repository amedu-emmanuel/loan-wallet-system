package com.koins.loanwallet.dto.response;

import com.koins.loanwallet.enums.TransactionStatus;
import com.koins.loanwallet.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID userId;
    private UUID walletId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus transactionStatus;
    private String referenceNumber;
    private LocalDateTime timestamp;
}