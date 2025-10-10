package com.paystream.paymentservice.app.command;

import java.math.BigDecimal;

public record RefundCommand(
        String paymentId,
        BigDecimal amount,
        String idempotencyKey
) { }
