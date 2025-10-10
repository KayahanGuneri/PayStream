package com.paystream.paymentservice.infra.dao;

import com.paystream.paymentservice.app.port.PaymentRepository;
import com.paystream.paymentservice.common.exception.DuplicateRequestException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository // Data access component for payments using JDBC
public class JdbcPaymentRepository implements PaymentRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcPaymentRepository(NamedParameterJdbcTemplate jdbc) {
        // DIP: infrastructure detail injected by Spring
        this.jdbc = jdbc;
    }

    private static final RowMapper<PaymentRecord> PAYMENT_MAPPER = new RowMapper<>() {
        @Override public PaymentRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Map row to a lightweight record to avoid leaking ORM entities
            return new PaymentRecord(
                    rs.getString("id"),
                    rs.getString("merchant_id"),
                    rs.getBigDecimal("amount"),
                    rs.getString("currency"),
                    rs.getString("status"),
                    rs.getString("card_token"),
                    (Integer) rs.getObject("risk_score")
            );
        }
    };

    @Override
    public UUID create(String merchantId, BigDecimal amount, String currency,
                       String cardToken, String idempotencyKey, String initialStatus, Instant now) {
        // Use explicit id to keep idempotent upserts simple (RETURNING keeps it atomic)
        final String sql = """
            INSERT INTO payments
              (id, merchant_id, amount, currency, status, card_token, risk_score,
               idempotency_key, version, created_at, updated_at)
            VALUES
              (:id, :mid, :amount, :cur, :status, :token, NULL, :idem, 0, :now, :now)
            RETURNING id
            """;

        UUID id = UUID.randomUUID();
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("mid", UUID.fromString(merchantId))
                .addValue("amount", amount)
                .addValue("cur", currency)
                .addValue("status", initialStatus)
                .addValue("token", cardToken)
                .addValue("idem", idempotencyKey)
                .addValue("now", now);
        try {
            return jdbc.queryForObject(sql, p, UUID.class);
        } catch (DuplicateKeyException e) {
            // Unique (merchant_id, idempotency_key) violated → map to domain exception
            throw new DuplicateRequestException("Idempotency conflict for merchantId=" + merchantId);
        }
    }

    @Override
    public Optional<PaymentRecord> findById(String paymentId) {
        final String sql = """
            SELECT id, merchant_id, amount, currency, status, card_token, risk_score
            FROM payments WHERE id = :id
            """;
        return jdbc.query(sql,
                new MapSqlParameterSource("id", UUID.fromString(paymentId)),
                PAYMENT_MAPPER).stream().findFirst();
    }

    @Override
    public Optional<PaymentRecord> findByIdemKey(String merchantId, String idempotencyKey) {
        final String sql = """
            SELECT id, merchant_id, amount, currency, status, card_token, risk_score
            FROM payments WHERE merchant_id = :mid AND idempotency_key = :idem
            """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("mid", UUID.fromString(merchantId))
                .addValue("idem", idempotencyKey);
        return jdbc.query(sql, p, PAYMENT_MAPPER).stream().findFirst();
    }

    @Override
    public void updateStatus(String paymentId, String newStatus, Instant now) {
        final String sql = """
            UPDATE payments
               SET status = :status, updated_at = :now, version = version + 1
             WHERE id = :id
            """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("status", newStatus)
                .addValue("now", now)
                .addValue("id", UUID.fromString(paymentId)));
    }
}
