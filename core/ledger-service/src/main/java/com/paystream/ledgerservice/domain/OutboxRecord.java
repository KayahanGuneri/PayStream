package com.paystream.ledgerservice.domain;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class OutboxRecord {
    // Unique id for the outbox row
    private UUID id;

    // Event type, e.g. "ledger.entry.appended"
    private String eventType;

    // Kafka partition key; we choose accountId to ensure in-partition ordering per account
    private UUID keyAccountId;

    // Serialized event payload (typically JSON)
    private String payload;

    // Timestamps for tracing and relay state
    private OffsetDateTime createdAt;
    private OffsetDateTime publishedAt;
}
