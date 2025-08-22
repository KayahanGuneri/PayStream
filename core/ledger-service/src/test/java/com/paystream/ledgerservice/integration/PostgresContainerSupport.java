package com.paystream.ledgerservice.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test") // -> application-test.yml aktif olsun
public abstract class PostgresContainerSupport {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("paystream")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        // ÖNEMLİ: Testlerde ContainerDatabaseDriver yerine normal PG driver'ı kullan
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        r.add("spring.flyway.enabled", () -> true);

        // Testte dış yan etkileri kapat
        r.add("ledger.outbox.relay.enabled", () -> false);
        r.add("ledger.snapshot.consumer.enabled", () -> false);
    }
}
