package com.paystream.paymentservice.app.command;

import java.math.BigDecimal;

public record AuthorizeCommand(
        String merchantId,
        BigDecimal amount,
        String currency,
        String cardToken,
        String idempotencyKey
) {}
