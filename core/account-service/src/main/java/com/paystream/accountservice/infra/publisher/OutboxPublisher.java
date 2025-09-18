package com.paystream.accountservice.infra.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Minimal outbox relay (batch). */
@EnableScheduling
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;

    @Scheduled(fixedDelay = 1000)
    public void pump() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT id, event_type, payload::text AS payload
              FROM outbox_events
             WHERE published_at IS NULL
             ORDER BY occurred_at
             LIMIT 100
        """);

        for (Map<String,Object> r : rows) {
            UUID id = (UUID) r.get("id");
            String eventType = (String) r.get("event_type");
            String payload = (String) r.get("payload");

            kafka.send(eventType, payload);
            jdbc.update("UPDATE outbox_events SET published_at = now() WHERE id = ?", id);
        }
    }
}
