package com.paystream.paymentservice.infra.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcPaymentIntentRepository.class)
@org.springframework.test.context.TestPropertySource(properties = {
        "spring.flyway.enabled=false"
})
class JdbcPaymentIntentRepositoryIT {

    @Autowired
    JdbcPaymentIntentRepository repo;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Test
    void insertAuthorizeIntent_shouldInsertRow() {
        var paymentId = UUID.randomUUID();

        repo.insertAuthorizeIntent(paymentId, new BigDecimal("100.00"), true, "RBA_TEST");

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM payment.payment_intents WHERE payment_id = ?",
                Integer.class,
                paymentId
        );

        assertThat(count).isEqualTo(1);
    }
}
