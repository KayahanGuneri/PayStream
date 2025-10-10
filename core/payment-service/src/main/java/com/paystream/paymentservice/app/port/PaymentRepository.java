package com.paystream.paymentservice.app.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

// Persistence abstraction for payments
public interface PaymentRepository {
    UUID create(String merchantId, BigDecimal amount, String currency,
                String cardToken, String idempotencyKey, String initialStatus, Instant now);

    Optional<PaymentRecord> findById(String paymentId);
    Optional<PaymentRecord> findByIdemKey(String merchantId, String idempotencyKey);
    void updateStatus(String paymentId, String newStatus, Instant now);

    // Lightweight record to avoid leaking ORM entities
    record PaymentRecord(String id, String merchantId, BigDecimal amount, String currency,
                         String status, String cardToken, Integer riskScore) { }
}
