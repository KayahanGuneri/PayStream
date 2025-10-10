package com.paystream.paymentservice.app.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Stores capture/refund records (a simple approach for skeleton)
public interface PaymentCaptureRepository {
    UUID create(String paymentId, BigDecimal amount, String status,
                String providerRef, String idempotencyKey, Instant now);
}
