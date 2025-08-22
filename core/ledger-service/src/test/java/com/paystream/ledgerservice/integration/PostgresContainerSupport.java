package com.paystream.ledgerservice.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles("test") // application-test.yml aktif
public abstract class PostgresContainerSupport {

    // JUnit ile değil, biz doğrudan yöneteceğiz
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("paystream")
                    .withUsername("postgres")
                    .withPassword("postgres");

    // >>> ERKEN BAŞLATMA: class yüklenir yüklenmez container başlasın
    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // Flyway ve yan etkileri testte aç/kapat
        r.add("spring.flyway.enabled", () -> true);
        r.add("spring.task.scheduling.enabled", () -> "false");
        r.add("ledger.outbox.relay.enabled", () -> false);
        r.add("ledger.snapshot.consumer.enabled", () -> false);
    }
}
