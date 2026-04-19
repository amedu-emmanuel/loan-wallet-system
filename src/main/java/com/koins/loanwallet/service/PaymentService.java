package com.koins.loanwallet.service;

import com.koins.loanwallet.dto.request.PaymentWebhookRequest;

public interface PaymentService {

    void processPaymentWebhook(PaymentWebhookRequest request);
}