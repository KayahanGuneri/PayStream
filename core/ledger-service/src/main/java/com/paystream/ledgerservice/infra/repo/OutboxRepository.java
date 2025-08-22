// OutboxRepository.java
package com.paystream.ledgerservice.infra.repo;

import com.paystream.ledgerservice.domain.OutboxRecord;
import com.paystream.ledgerservice.infra.mapper.OutboxRecordRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;   // <<< eklendi
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxRepository {

    private final JdbcTemplate jdbc;
    private final OutboxRecordRowMapper rowMapper;

    public void insert(OutboxRecord rec) {
        final String sql = """
            INSERT INTO ledger_outbox(id, event_type, key_account_id, payload, created_at, published_at)
            VALUES (?, ?, ?, ?::jsonb, now(), NULL)
            RETURNING created_at
        """;
        OffsetDateTime createdAt = jdbc.queryForObject(
                sql,
                OffsetDateTime.class,
                rec.getId(), rec.getEventType(), rec.getKeyAccountId(), rec.getPayload()
        );
        // testin beklediği alanı dolduruyoruz
        rec.setCreatedAt(createdAt);
        rec.setPublishedAt(null);
    }

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

    public void markPublished(UUID id) {
        jdbc.update("UPDATE ledger_outbox SET published_at = now() WHERE id = ?", id);
    }
}
