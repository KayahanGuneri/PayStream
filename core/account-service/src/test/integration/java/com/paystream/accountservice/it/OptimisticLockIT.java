package com.paystream.accountservice.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OptimisticLockIT {

    // --- Real Postgres for integration testing (Testcontainers) ---
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("paystream")
                    .withUsername("postgres")
                    .withPassword("postgres");

    // Wire Testcontainers JDBC settings into Spring
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        // Run Flyway on test DB
        r.add("spring.flyway.enabled", () -> "true");

        // Optional: disable discovery in tests (if you use Eureka etc.)
        r.add("eureka.client.enabled", () -> "false");

        // Optional: if your app boots as servlet by default, you can keep or drop this
        r.add("spring.main.web-application-type", () -> "servlet");
    }

    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("Two concurrent updates with the same expected version -> only one succeeds (version increments exactly once)")
    void optimisticLock_shouldAllowOnlyOneWinner() throws Exception {
        // 1) Insert a fresh account row so 'version' starts at 0
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        // Adjust columns if your schema differs (status/version/created_at/updated_at)
        jdbc.update("""
            INSERT INTO accounts(id, customer_id, currency, status, version, created_at, updated_at)
            VALUES (?::uuid, ?::uuid, 'TRY', 'ACTIVE', 0, NOW(), NOW())
        """, accountId, customerId);

        // Sanity check: version = 0
        Long v0 = jdbc.queryForObject(
                "SELECT version FROM accounts WHERE id=?::uuid",
                Long.class, accountId.toString());
        assertThat(v0).isEqualTo(0L);

        // 2) Prepare two concurrent updates targeting the SAME expected version (=0)
        //    The SQL implements optimistic locking by matching current version in WHERE
        String sqlOptimisticUpdate = """
            UPDATE accounts
               SET status = ?,
                   version = version + 1,
                   updated_at = NOW()
             WHERE id = ?::uuid
               AND version = ?
        """;

        var pool = Executors.newFixedThreadPool(2);
        var startGate = new CountDownLatch(1);
        var winners = new AtomicInteger(0);

        Runnable task = () -> {
            try {
                startGate.await(); // release both at once
                int updated = jdbc.update(sqlOptimisticUpdate, "FROZEN", accountId.toString(), 0L);
                if (updated == 1) {
                    winners.incrementAndGet();
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        };

        Future<?> f1 = pool.submit(task);
        Future<?> f2 = pool.submit(task);

        // Fire!
        startGate.countDown();
        f1.get();
        f2.get();
        pool.shutdown();

        // Exactly one thread must have succeeded
        assertThat(winners.get()).isEqualTo(1);

        // 3) Verify: final version must be 1 (incremented exactly once), status 'FROZEN'
        Long version = jdbc.queryForObject(
                "SELECT version FROM accounts WHERE id=?::uuid", Long.class, accountId.toString());
        String status = jdbc.queryForObject(
                "SELECT status FROM accounts WHERE id=?::uuid", String.class, accountId.toString());

        assertThat(version).isEqualTo(1L);
        assertThat(status).isEqualTo("FROZEN");
    }
}
