package com.paystream.ledgerservice.infra.repo;

import com.paystream.ledgerservice.domain.OutboxRecord;
import com.paystream.ledgerservice.infra.mapper.OutboxRecordRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository // Dedicated to SQL; mapping delegated to RowMapper
@RequiredArgsConstructor
public class OutboxRepository {

    private final JdbcTemplate jdbc;                  // JDBC access
    private final OutboxRecordRowMapper rowMapper;    // DB→domain mapping

    // Saves an unpublished outbox record
    public void insert(OutboxRecord rec) {
        final String sql = """
            INSERT INTO ledger_outbox(id, event_type, key_account_id, payload, created_at, published_at)
            VALUES (?, ?, ?, ?::jsonb, now(), NULL)
        """;
        jdbc.update(sql, rec.getId(), rec.getEventType(), rec.getKeyAccountId(), rec.getPayload());
    }

    // Fetches a batch of unpublished records (FIFO-ish by created_at)
    public List<OutboxRecord> fetchUnpublishedBatch(int limit) {
        final String sql = """
            SELECT id, event_type, key_account_id, payload, created_at, published_at
            FROM ledger_outbox
            WHERE published_at IS NULL
            ORDER BY created_at ASC
            LIMIT ?
        """;
        return jdbc.query(sql, rowMapper, limit);
    }

    // Marks a record as published; relay will skip it next time
    public void markPublished(UUID id) {
        jdbc.update("UPDATE ledger_outbox SET published_at = now() WHERE id = ?", id);
    }
}
