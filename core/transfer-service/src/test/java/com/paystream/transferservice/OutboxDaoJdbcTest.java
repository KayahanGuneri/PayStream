package com.paystream.transferservice;

import com.paystream.transferservice.infra.dao.OutboxDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
class OutboxDaoJdbcTest {

    // Deterministic ve lokal erişim için Testcontainers
    @Container
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("paystream")
            .withUsername("test")
            .withPassword("test");

    // Spring’e DATA SOURCE’u İLK ELDEN buradan enjekte ettiriyoruz
    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", PG::getJdbcUrl);
        reg.add("spring.datasource.username", PG::getUsername);
        reg.add("spring.datasource.password", PG::getPassword);
        // Flyway aynı datasource’u kullanır; ayrı ayar gerektirmez.
        // İsteğe bağlı: Hikari + Flyway için timeouts vs. gerekmez.
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired OutboxDao outboxDao;

    @Test
    @DisplayName("append writes NEW event with non-empty payload and correct UUIDs")
    void append_writes_new_event() {
        UUID aggregateId = UUID.randomUUID();
        UUID keyAccId   = UUID.randomUUID();

        String payload = """
            {"event":"TRANSFER_COMPLETED","amount":1000,"currency":"TRY"}
            """;

        outboxDao.append("TRANSFER", aggregateId, keyAccId, payload);

        // Kolon ismi: payload_json (jsonb). Okurken text'e çevir.
        String storedPayload = jdbc.queryForObject(
                "select payload_json::text from outbox_events where aggregate_id = ? order by created_at desc limit 1",
                String.class,
                aggregateId
        );
        String status = jdbc.queryForObject(
                "select status from outbox_events where aggregate_id = ? order by created_at desc limit 1",
                String.class,
                aggregateId
        );
        UUID storedAggId = jdbc.queryForObject(
                "select aggregate_id from outbox_events where aggregate_id = ? order by created_at desc limit 1",
                UUID.class,
                aggregateId
        );
        UUID storedKeyAccId = jdbc.queryForObject(
                "select key_account_id from outbox_events where aggregate_id = ? order by created_at desc limit 1",
                UUID.class,
                aggregateId
        );

        assertThat(storedPayload).isNotBlank();
        assertThat(storedPayload).contains("\"TRANSFER_COMPLETED\"");
        assertThat(status).isEqualTo("NEW");
        assertThat(storedAggId).isEqualTo(aggregateId);
        assertThat(storedKeyAccId).isEqualTo(keyAccId);
    }
}
