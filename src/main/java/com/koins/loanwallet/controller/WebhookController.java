package com.koins.loanwallet.controller;

import com.koins.loanwallet.dto.request.PaymentWebhookRequest;
import com.koins.loanwallet.dto.response.ApiResponse;
import com.koins.loanwallet.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ApiResponse<Void> processPaymentWebhook(@Valid @RequestBody PaymentWebhookRequest request) {
        paymentService.processPaymentWebhook(request);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Webhook processed successfully")
                .data(null)
                .build();
    }
}