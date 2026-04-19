package com.koins.loanwallet.util;

import java.util.UUID;

public final class ReferenceGenerator {

    private ReferenceGenerator() {
    }

    public static String generateReference(String prefix) {
        return prefix + "-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }
}