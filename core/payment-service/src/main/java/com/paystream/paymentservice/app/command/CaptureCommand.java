package com.paystream.paymentservice.app.command;

import java.math.BigDecimal;

public record CaptureCommand (
        String paymentId,
        BigDecimal amount,
        String idempotencyKey
){ }
