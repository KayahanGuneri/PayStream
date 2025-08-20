package com.paystream.accountservice.it.com.paystream.accountservice.it;
import com.paystream.accountservice.infra.dao.outbox.OutboxDao;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class TransactionRollbackIT {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("paystream")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static { POSTGRES.start(); }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("eureka.client.enabled", () -> false);
        r.add("spring.flyway.enabled", () -> true);
    }

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @SpyBean OutboxDao outboxDao;

    @Test
    @DisplayName("If outbox insertion fails, the whole createAccount transaction rolls back")
    void allOrNothing() throws Exception {
        long accBefore    = jdbc.queryForObject("select count(*) from accounts", Long.class);
        long balBefore    = jdbc.queryForObject("select count(*) from account_balances", Long.class);
        long outboxBefore = jdbc.queryForObject("select count(*) from outbox_events", Long.class);

        // Force an exception inside the same transaction
        Mockito.doThrow(new RuntimeException("boom"))
                .when(outboxDao).insert(any());

        String body = """
          {"customerId":"11111111-1111-1111-1111-111111111111","currency":"TRY"}
        """;

        // IMPORTANT: endpoint path must be /accounts (plural)
        assertThatThrownBy(() ->
                mvc.perform(post("/accounts")
                                .contentType("application/json")
                                .header("x-trace-id","it-rollback")
                                .content(body))
                        .andReturn()
        )
                .isInstanceOf(ServletException.class)              // MockMvc wraps into ServletException
                .hasRootCauseInstanceOf(RuntimeException.class);   // our "boom" is the root cause

        long accAfter    = jdbc.queryForObject("select count(*) from accounts", Long.class);
        long balAfter    = jdbc.queryForObject("select count(*) from account_balances", Long.class);
        long outboxAfter = jdbc.queryForObject("select count(*) from outbox_events", Long.class);

        org.assertj.core.api.Assertions.assertThat(accAfter).isEqualTo(accBefore);
        org.assertj.core.api.Assertions.assertThat(balAfter).isEqualTo(balBefore);
        org.assertj.core.api.Assertions.assertThat(outboxAfter).isEqualTo(outboxBefore);
    }
}
