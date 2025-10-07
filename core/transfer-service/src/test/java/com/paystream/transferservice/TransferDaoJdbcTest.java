package com.paystream.transferservice;

import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import com.paystream.transferservice.infra.dao.TransferDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@JdbcTest // start JDBC slice with JdbcTemplate
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // do not swap with H2
@Testcontainers // enable Testcontainers JUnit 5 integration
@Import(TransferDao.class) // register our DAO bean into the test slice
class TransferDaoJdbcTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("paystream")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        // Wire Spring’s DataSource to the running container
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
        // Make Flyway run V1__*, V2__* on startup
        r.add("spring.flyway.enabled", () -> true);
        r.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired TransferDao dao;

    // Helper: build a PENDING transfer
    private static Transfer newPending(String key) {
        var t = new Transfer();
        t.id = UUID.randomUUID();
        t.sourceAccountId = UUID.randomUUID();
        t.destAccountId = UUID.randomUUID();
        t.currency = "TRY";
        t.amountMinor = 1000L;
        t.idempotencyKey = key;
        t.status = TransferStatus.PENDING;
        return t;
    }

    @Test
    @DisplayName("insertPending -> row exists and is PENDING")
    void insert_and_find() {
        var t = newPending("K-1");
        dao.insertPending(t);

        Optional<Transfer> found = dao.findById(t.id);
        assertThat(found).isPresent();
        assertThat(found.get().status).isEqualTo(TransferStatus.PENDING);
        assertThat(found.get().idempotencyKey).isEqualTo("K-1");
    }

    @Test
    @DisplayName("updateStatus -> IN_PROGRESS")
    void update_status() {
        var t = newPending("K-2");
        dao.insertPending(t);

        dao.updateStatus(t.id, TransferStatus.IN_PROGRESS);
        var found = dao.findById(t.id).orElseThrow();
        assertThat(found.status).isEqualTo(TransferStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("markCompleted sets COMPLETED + ledgerTxId")
    void mark_completed() {
        var t = newPending("K-3");
        dao.insertPending(t);

        var ledgerTx = UUID.randomUUID();
        dao.markCompleted(t.id, ledgerTx);

        var found = dao.findById(t.id).orElseThrow();
        assertThat(found.status).isEqualTo(TransferStatus.COMPLETED);
        assertThat(found.ledgerTxId).isEqualTo(ledgerTx);
    }

    @Test
    @DisplayName("markFailed -> FAILED")
    void mark_failed() {
        var t = newPending("K-4");
        dao.insertPending(t);

        dao.markFailed(t.id);
        var found = dao.findById(t.id).orElseThrow();
        assertThat(found.status).isEqualTo(TransferStatus.FAILED);
    }

    @Test
    @DisplayName("findByIdempotencyKey returns the same row")
    void find_by_key() {
        var t = newPending("K-5");
        dao.insertPending(t);

        var byKey = dao.findByIdempotencyKey("K-5");
        assertThat(byKey).isPresent();
        assertThat(byKey.get().id).isEqualTo(t.id);
    }

    @Test
    @DisplayName("UNIQUE(idempotency_key): second insert with same key fails")
    void unique_idempotency_key() {
        var t1 = newPending("K-6");
        var t2 = newPending("K-6"); // same key

        dao.insertPending(t1);
        assertThatThrownBy(() -> dao.insertPending(t2))
                .isInstanceOf(Exception.class); // translated to DataIntegrityViolationException normally
    }
}
