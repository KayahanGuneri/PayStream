package com.paystream.paymentservice.infra.dao;

import com.paystream.paymentservice.domain.PaymentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JdbcPaymentRepository {

    private final JdbcTemplate jdbc;

    public JdbcPaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * payments tablosuna yeni kayıt ekler.
     * Şema: payment.payments(id, merchant_id, amount, currency, status, card_token, idempotency_key, created_at, updated_at)
     */
    public void savePayment(UUID id,
                            UUID merchantId,
                            BigDecimal amount,
                            String currency,
                            PaymentStatus status,
                            String cardToken,
                            String idempotencyKey) {

        jdbc.update("""
            INSERT INTO payment.payments
              (id, merchant_id, amount, currency, status, card_token, idempotency_key, created_at, updated_at)
            VALUES
              (?,  ?,           ?,      ?,        ?,      ?,          ?,               now(),    now())
            """,
                id,
                merchantId,                 // UUID tipinde
                amount,
                currency,
                status.name(),
                cardToken,
                idempotencyKey
        );
    }

    /**
     * payments.status alanını günceller.
     */
    public void updateStatus(UUID id, PaymentStatus nextStatus) {
        jdbc.update("""
            UPDATE payment.payments
               SET status = ?, updated_at = now()
             WHERE id = ?
            """,
                nextStatus.name(),
                id
        );
    }

    /**
     * Tek bir ödemeyi id ile döndürür.
     */
    public Optional<PaymentRecord> findById(UUID id) {
        return jdbc.query("""
                SELECT id,
                       merchant_id,
                       amount,
                       currency,
                       status,
                       card_token,
                       idempotency_key,
                       created_at
                  FROM payment.payments
                 WHERE id = ?
                """, MAPPER, id
        ).stream().findFirst();
    }

    // RowMapper
    private static final RowMapper<PaymentRecord> MAPPER = (ResultSet rs, int rowNum) -> new PaymentRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("merchant_id", UUID.class),
            rs.getBigDecimal("amount"),
            rs.getString("currency"),
            PaymentStatus.valueOf(rs.getString("status")),
            rs.getString("card_token"),
            rs.getString("idempotency_key"),
            rs.getTimestamp("created_at").toInstant()
    );

    public record PaymentRecord(
            UUID id,
            UUID merchantId,
            BigDecimal amount,
            String currency,
            PaymentStatus status,
            String cardToken,
            String idempotencyKey,
            Instant createdAt
    ) {}
}
