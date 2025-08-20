package com.paystream.accountservice.domain.outbox;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        String aggregateType,
        UUID aggregateId,
        String eventType,
        Map<String, Object> payload,
        Map<String, String> headers,
        Instant occurredAt,
        Object o) {}
