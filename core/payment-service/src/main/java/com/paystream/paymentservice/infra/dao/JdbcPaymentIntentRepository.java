package com.paystream.paymentservice.infra.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public class JdbcPaymentIntentRepository {

    private final JdbcTemplate jdbc;

    public JdbcPaymentIntentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertAuthorizeIntent(UUID paymentId,
                                      BigDecimal requestedAmount,
                                      boolean requires3ds,
                                      String rbaReason) {
        UUID intentId = UUID.randomUUID();

        jdbc.update("""
            INSERT INTO payment.payment_intents
              (id, payment_id, requested_amount, requires_3ds, rba_reason, created_at)
            VALUES
              (?,  ?,          ?,                ?,            ?,          now())
            """,
                intentId,
                paymentId,
                requestedAmount,
                requires3ds,
                rbaReason
        );
    }
}
