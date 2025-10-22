package com.paystream.paymentservice.infra.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JdbcOutboxPort {

    private final JdbcTemplate jdbc;

    @Transactional(propagation = Propagation.MANDATORY)
    public UUID publishEvent(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String payloadJson,
            String metadataJson
    ) {
        try {
            final String sql = """
                INSERT INTO payment.outbox_events
                  (id, aggregate_type, aggregate_id, event_type, payload, metadata, headers, occurred_at, status, published, created_at)
                VALUES
                  (?::uuid, ?, ?::uuid, ?, ?::jsonb, ?::jsonb, NULL, now(), 'NEW', FALSE, now())
                RETURNING id
                """;

            // 1) id'yi üret
            UUID id = UUID.randomUUID();

            // 2) queryForObject'a 6 parametreyi DOĞRU sırayla ver
            return jdbc.queryForObject(
                    sql,
                    (rs, rowNum) -> (UUID) rs.getObject(1, java.util.UUID.class),
                    id,               // <-- id (1. placeholder)
                    aggregateType,    // aggregate_type
                    aggregateId,      // aggregate_id
                    eventType,        // event_type
                    payloadJson,      // payload  (geçerli JSON string olmalı)
                    metadataJson      // metadata (null olabilir -> NULL::jsonb olarak cast edilir)
            );

        } catch (DataAccessException ex) {
            throw ex;
        }
    }

    @Transactional
    public int markPublished(UUID outboxId) {
        final String sql = """
            UPDATE payment.outbox_events
               SET status='PUBLISHED', published=TRUE, published_at = now()
             WHERE id=?::uuid AND status='NEW'
            """;
        return jdbc.update(sql, outboxId);
    }

    @Transactional
    public int markFailed(UUID outboxId, String errorMessage) {
        final String sql = """
            UPDATE payment.outbox_events
               SET status='FAILED', error=?
             WHERE id=?::uuid AND status='NEW'
            """;
        return jdbc.update(sql, errorMessage, outboxId);
    }

    @Transactional(readOnly = true)
    public Optional<UUID> exists(UUID outboxId) {
        final String sql = "SELECT id FROM payment.outbox_events WHERE id=?::uuid";
        return jdbc.query(sql,
                rs -> rs.next() ? Optional.of((UUID) rs.getObject("id")) : Optional.empty(),
                outboxId);
    }
}
