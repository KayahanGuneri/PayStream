package com.paystream.paymentservice.infra.dao;

import com.paystream.paymentservice.app.port.PaymentCaptureRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcPaymentCaptureRepository implements PaymentCaptureRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcPaymentCaptureRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UUID create(String paymentId, BigDecimal amount, String status,
                       String providerRef, String idempotencyKey, Instant now) {
        final String sql = """
            INSERT INTO payment_captures
              (id, payment_id, amount, status, provider_ref, idempotency_key, created_at)
            VALUES
              (:id, :pid, :amount, :status, :pref, :idem, :now)
            RETURNING id
            """;
        UUID id = UUID.randomUUID();
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("pid", UUID.fromString(paymentId))
                .addValue("amount", amount)
                .addValue("status", status)
                .addValue("pref", providerRef)
                .addValue("idem", idempotencyKey)
                .addValue("now", now);
        return jdbc.queryForObject(sql, p, UUID.class);
    }
}
