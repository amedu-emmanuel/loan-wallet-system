package com.koins.loanwallet.entity;

import com.koins.loanwallet.enums.LoanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "loans")
public class Loan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_loan_user"))
    private User user;

    @Column(name = "loan_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "loan_duration_days", nullable = false)
    private Integer loanDurationDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_status", nullable = false, length = 20)
    private LoanStatus loanStatus;

    @Column(name = "repayment_schedule", columnDefinition = "TEXT")
    private String repaymentSchedule;

    @Column(name = "total_repayable_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalRepayableAmount;

    @Column(name = "amount_repaid", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountRepaid;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "disbursed_at")
    private java.time.LocalDateTime disbursedAt;

    @Column(name = "repaid_at")
    private java.time.LocalDateTime repaidAt;
}