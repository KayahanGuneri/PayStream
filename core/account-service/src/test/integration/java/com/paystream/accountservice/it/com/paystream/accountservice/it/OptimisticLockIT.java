package com.paystream.accountservice.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.accountservice.infra.dao.AccountDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OptimisticLockIT {

    // --- Real Postgres for integration testing ---
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("paystream")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static { POSTGRES.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // Wire Testcontainers JDBC settings into Spring
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        // Disable Eureka during tests
        r.add("eureka.client.enabled", () -> "false");
        // Run Flyway on test DB
        r.add("spring.flyway.enabled", () -> "true");
        // Force servlet stack
        r.add("spring.main.web-application-type", () -> "servlet");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper om;
    @Autowired AccountDao accountDao;

    @Test
    @DisplayName("Two concurrent updates with same expected version -> only one succeeds")
    void optimisticLock_shouldAllowOnlyOneWinner() throws Exception {
        // 1) Insert an account row quickly (bypass HTTP) so 'version' starts at 0
        //    We'll reuse your DAO schema: status='ACTIVE', version=0
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO accounts(id, customer_id, currency, status, version, created_at, updated_at)
                VALUES (?::uuid, ?::uuid, 'TRY', 'ACTIVE', 0, NOW(), NOW())
            """, id, UUID.randomUUID());

        // 2) Prepare two concurrent updates targeting the SAME version (=0)
        //    Only one should update row count=1; the other should get 0 due to version mismatch
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1); // release both at once

        Runnable r1 = () -> {
            try {
                start.await(); // wait until both are ready
                // expectedVersion=0 -> one of the threads will win
                accountDao.updateStatusWithOptimisticLock(id, "FROZEN", 0L);
            } catch (InterruptedException ignored) {}
        };
        Runnable r2 = () -> {
            try {
                start.await();
                accountDao.updateStatusWithOptimisticLock(id, "FROZEN", 0L);
            } catch (InterruptedException ignored) {}
        };

        Future<?> f1 = pool.submit(r1);
        Future<?> f2 = pool.submit(r2);
        start.countDown(); // release both

        f1.get();
        f2.get();
        pool.shutdown();

        // 3) Verify: final version must be 1 (incremented exactly once), status 'FROZEN'
        Long version = jdbc.queryForObject(
                "SELECT version FROM accounts WHERE id=?::uuid", Long.class, id.toString());
        String status = jdbc.queryForObject(
                "SELECT status FROM accounts WHERE id=?::uuid", String.class, id.toString());

        // -- Assertions --
        // Only one update should have succeeded -> version bumped once
        assertThat(version).isEqualTo(1L);
        // Final status must be FROZEN
        assertThat(status).isEqualTo("FROZEN");
    }
}
