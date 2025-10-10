package com.paystream.paymentservice.infra.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.paymentservice.app.port.OutboxPort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Repository
public class JdbcOutboxPort implements OutboxPort{

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public JdbcOutboxPort(NamedParameterJdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public void appendEvent(String aggregateType, String aggregateId,
                            String eventType, Map<String, Object> payload, Instant occurredAt) {
        final String sql = """
            INSERT INTO outbox_events
              (id, aggregate_type, aggregate_id, event_type, payload, headers, occurred_at, status)
            VALUES
              (:id, :atype, :aid, :etype, CAST(:payload AS JSONB), NULL, :occurredAt, 'NEW')
            """;

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize outbox payload", e);
        }

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("atype", aggregateType)
                .addValue("aid", UUID.fromString(aggregateId))
                .addValue("etype", eventType)
                .addValue("payload", payloadJson)
                .addValue("occurredAt", occurredAt);

        jdbc.update(sql, p);
    }
}
