package com.paystream.paymentservice.infra.dao;

import com.paystream.paymentservice.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PURPOSE:
 * Integration-test for JdbcPaymentRepository using a real PostgreSQL (Testcontainers).
 * We verify savePayment(...), findById(...), and updateStatus(...) work end-to-end.
 *
 * NOTES for beginners:
 * - We insert a row, read it back, then update status and read again.
 */
@JdbcTest
@ActiveProfiles("test")
@Import(JdbcPaymentRepository.class)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JdbcPaymentRepositoryIT {

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

    @Autowired JdbcPaymentRepository repo;

    @Test
    @DisplayName("savePayment() then findById() returns inserted row; updateStatus() changes status")
    void save_find_updateStatus() {
        // Given: data for a new payment
        var id = UUID.randomUUID();
        var merchantId = UUID.randomUUID();
        var amount = new BigDecimal("99.50");
        var currency = "TRY";
        var status = PaymentStatus.NEW;
        var cardToken = "card-t-xyz";
        var idemKey = "idem-abc";

        // When: insert
        repo.savePayment(
                id,
                merchantId,
                amount,
                currency,
                status,
                cardToken,
                idemKey
        );

        // Then: read it back
        var found = repo.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(id);
        assertThat(found.get().merchantId()).isEqualTo(merchantId);
        assertThat(found.get().amount()).isEqualByComparingTo("99.50");
        assertThat(found.get().currency()).isEqualTo("TRY");
        assertThat(found.get().status()).isEqualTo(PaymentStatus.NEW);
        assertThat(found.get().cardToken()).isEqualTo("card-t-xyz");
        assertThat(found.get().idempotencyKey()).isEqualTo("idem-abc");

        // And: update status
        repo.updateStatus(id, PaymentStatus.AUTH_APPROVED);

        var updated = repo.findById(id);
        assertThat(updated).isPresent();
        assertThat(updated.get().status()).isEqualTo(PaymentStatus.AUTH_APPROVED);
    }
}
