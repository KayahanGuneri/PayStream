// Appends rows to outbox_events to be published by a relay later.
package com.paystream.transferservice.infra.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class OutboxDao {
    private final JdbcTemplate jdbc;
    public OutboxDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void append(String aggregateType, String aggregateId, UUID keyAccountId, String payloadJson) {
        jdbc.update("""
            INSERT INTO outbox_events(aggregate_type, aggregate_id, key_account_id, payload, status)
            VALUES (?, ?, ?, ?::jsonb, 'NEW')
        """, aggregateType, aggregateId, keyAccountId, payloadJson);
    }
}

