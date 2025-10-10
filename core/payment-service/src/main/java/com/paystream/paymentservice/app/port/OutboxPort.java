package com.paystream.paymentservice.app.port;

import java.time.Instant;
import java.util.Map;

// Outbox pattern abstraction (append-only)
public interface OutboxPort {
    void appendEvent(String aggregateType, String aggregateId,
                     String eventType, Map<String, Object> payload, Instant occurredAt);
}
