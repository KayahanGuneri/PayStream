package com.paystream.accountservice.infra.dao.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.accountservice.domain.outbox.OutboxEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public class OutboxDao {
    private final JdbcTemplate jdbc;
    private final ObjectMapper om;

    public OutboxDao(JdbcTemplate jdbc, ObjectMapper om) {
        this.jdbc = jdbc;
        this.om = om;
    }

    public void insert(OutboxEvent e) {
        String sql = """
            INSERT INTO outbox_events
            (id, aggregate_type, aggregate_id, event_type, payload, headers, occurred_at, published_at)
            VALUES (?,?,?,?, ?::jsonb, ?::jsonb, ?, NULL)
        """;
        String payloadJson = toJson(e.payload());
        String headersJson = toJson(e.headers());
        jdbc.update(sql,
                e.id(), e.aggregateType(), e.aggregateId(), e.eventType(),
                payloadJson, headersJson,
                Timestamp.from(e.occurredAt()));
    }

    private String toJson(Object value) {
        try { return om.writeValueAsString(value); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
}
