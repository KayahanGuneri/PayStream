package com.paystream.ledgerservice.integration;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public abstract class PostgresContainerSupport {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // Primary datasource
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        // Flyway aynı DB'yi kullansın
        r.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        r.add("spring.flyway.user", POSTGRES::getUsername);
        r.add("spring.flyway.password", POSTGRES::getPassword);

        // İKİNCİ DataSource kullanıyorsan (logda HikariPool-2 var):
        // property isimlerini projendekiyle birebir eşleştir.
        r.add("app.snapshot-datasource.url", POSTGRES::getJdbcUrl);
        r.add("app.snapshot-datasource.username", POSTGRES::getUsername);
        r.add("app.snapshot-datasource.password", POSTGRES::getPassword);
    }
}
