package com.koins.loanwallet.util;

import java.math.BigDecimal;

public final class AppConstants {

    private AppConstants() {
    }

    public static final String API_V1 = "/api/v1";
    public static final String DEFAULT_CURRENCY = "NGN";

    public static final BigDecimal DEFAULT_WALLET_BALANCE = BigDecimal.ZERO;
    public static final BigDecimal DEFAULT_INTEREST_RATE = new BigDecimal("5.00");

    public static final String WALLET_FUNDING_PREFIX = "FUND";
    public static final String LOAN_DISBURSEMENT_PREFIX = "LOAN";
    public static final String REPAYMENT_PREFIX = "REPAY";

    public static final int OTP_LENGTH = 6;
}