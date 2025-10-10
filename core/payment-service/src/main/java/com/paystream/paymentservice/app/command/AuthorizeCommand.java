package com.paystream.paymentservice.app.command;


import java.math.BigDecimal;


// Input model for authorize use-case (keeps API decoupled)
public record AuthorizeCommand(
        String merchantId,
        BigDecimal amount,
        String currency,
        String cardToken,
        String idempotencyKey
) { }
