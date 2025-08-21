package com.paystream.ledgerservice.infra.mapper;

import com.paystream.ledgerservice.domain.OutboxRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component // Keeps mappers cohesive and reusable across repositories
public class OutboxRecordRowMapper implements RowMapper<OutboxRecord> {

    @Override // Maps a JDBC row to an OutboxRecord domain object
    public OutboxRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        return OutboxRecord.builder()
                .id((UUID) rs.getObject("id"))                               // outbox row id
                .eventType(rs.getString("event_type"))                       // event discriminator
                .keyAccountId((UUID) rs.getObject("key_account_id"))         // Kafka partition key
                .payload(rs.getString("payload"))                            // JSON payload as string
                .createdAt(rs.getObject("created_at", OffsetDateTime.class)) // creation time
                .publishedAt(rs.getObject("published_at", OffsetDateTime.class)) // null if unpublished
                .build();
    }
}
