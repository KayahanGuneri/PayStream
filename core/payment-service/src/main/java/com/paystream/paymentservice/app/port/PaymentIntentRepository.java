package com.paystream.paymentservice.app.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Authorization intents
public interface PaymentIntentRepository {
    UUID create(String paymentId, BigDecimal requestedAmount, boolean requires3ds,
                String rbaReason, Instant now);
}
