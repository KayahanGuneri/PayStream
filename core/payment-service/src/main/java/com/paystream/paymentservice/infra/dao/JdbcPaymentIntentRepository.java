package com.paystream.paymentservice.infra.dao;

import com.paystream.paymentservice.app.port.PaymentIntentRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public class JdbcPaymentIntentRepository implements PaymentIntentRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcPaymentIntentRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UUID create(String paymentId, BigDecimal requestedAmount, boolean requires3ds,
                       String rbaReason, Instant now) {
        final String sql = """
            INSERT INTO payment_intents
              (id, payment_id, requested_amount, requires_3ds, rba_reason, created_at)
            VALUES
              (:id, :pid, :amount, :req3ds, :reason, :now)
            RETURNING id
            """;
        UUID id = UUID.randomUUID();
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("pid", UUID.fromString(paymentId))
                .addValue("amount", requestedAmount)
                .addValue("req3ds", requires3ds)
                .addValue("reason", rbaReason)
                .addValue("now", now);
        return jdbc.queryForObject(sql, p, UUID.class);
    }
}
