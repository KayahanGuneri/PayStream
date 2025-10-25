package com.paystream.paymentservice.infra.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PURPOSE:
 * Integration-test for JdbcOutboxPort with a real PostgreSQL (Testcontainers).
 * We verify publishEvent(...) inserts a NEW row into payment.outbox_events.
 *
 * NOTES for beginners:
 * - @JdbcTest starts a light Spring context with JDBC support.
 * - Testcontainers spins up a temporary Postgres for tests.
 * - Flyway runs db/migration scripts automatically.
 */
@JdbcTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdbcOutboxPort.class)
@Testcontainers
class JdbcOutboxPortIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> true);
        r.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired JdbcOutboxPort outbox;

    @Test
    @DisplayName("publishEvent() should insert a row with status NEW")
    void publish_inserts_row() {
        var id = outbox.publishEvent(
                UUID.randomUUID(),          // aggregateId
                "Payment",                  // aggregateType
                "PaymentAuthorized",        // eventType
                "{\"amount\":100}",         // payloadJson
                "{\"traceId\":\"t-1\"}"     // metadataJson
        );

        Integer c = jdbc.queryForObject(
                "SELECT count(*) FROM payment.outbox_events WHERE id = ?::uuid AND status='NEW'",
                Integer.class, id
        );
        assertThat(id).isNotNull();
        assertThat(c).isEqualTo(1);
    }
}
