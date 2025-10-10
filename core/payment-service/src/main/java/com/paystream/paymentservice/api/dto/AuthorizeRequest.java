package com.paystream.paymentservice.api.dto;

import java.math.BigDecimal;

// SRP: Carries only request data for authorization
public record AuthorizeRequest(
        String merchantId,
        BigDecimal amount, // BigDecimal for monetary precision (scale=2)
        String currency,   // ISO-4217
        String cardToken
) { }
