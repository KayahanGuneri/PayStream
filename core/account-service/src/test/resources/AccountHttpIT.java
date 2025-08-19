package com.paystream.accountservice.it.com.paystream.accountservice.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Boot the full Spring context (Servlet stack) so we test real configuration
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AccountHttpIT {


    // Reusable real Postgres for integration tests (migrations run for real)
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("paystream")
                    .withUsername("postgres")
                    .withPassword("postgres");

    // Start once per test class
    static { POSTGRES.start(); }

    // Wire Testcontainers JDBC settings into Spring at runtime
    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("eureka.client.enabled", () -> "false"); // avoid discovery during tests
        r.add("spring.flyway.enabled", () -> "true");  // run Flyway on the test DB
        r.add("spring.main.web-application-type", () -> "servlet");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("POST /accounts -> 200 and creates rows in accounts, account_balances, outbox_events")
    void createAccount_happyPath() throws Exception {
        // Request payload
        String body = """
      {"customerId":"11111111-1111-1111-1111-111111111111","currency":"TRY"}
    """;

        // Perform HTTP POST and validate basic fields
        String json = mvc.perform(post("/accounts")
                        .contentType("application/json")
                        .header("x-trace-id","it-1")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("TRY"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        // Extract generated account id from response
        JsonNode root = om.readTree(json);
        String accountId = root.get("id").asText();

        // Assert that 'accounts' row exists
        Integer accCount = jdbc.queryForObject(
                "select count(*) from accounts where id = ?::uuid", Integer.class, accountId);
        Assertions.assertEquals(1, accCount);

        // Assert that 'account_balances' row exists with 0.00
        Integer balCount = jdbc.queryForObject(
                "select count(*) from account_balances where account_id = ?::uuid", Integer.class, accountId);
        Assertions.assertEquals(1, balCount);

        String bal = jdbc.queryForObject(
                "select current_balance::text from account_balances where account_id = ?::uuid", String.class, accountId);
        Assertions.assertEquals("0.00", bal);

        // Two outbox events must exist (created + snapshot)
        Integer evCount = jdbc.queryForObject(
                "select count(*) from outbox_events where aggregate_id = ?::uuid", Integer.class, accountId);
        Assertions.assertEquals(2, evCount);

        // GET balance should return the snapshot
        mvc.perform(get("/accounts/{id}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.currentBalance").isNumber());

    }

    @Test
    @DisplayName("POST /accounts -> 400 when currency is missing (validation)")
    void createAccount_validationError() throws Exception {
        // currency omitted on purpose
        String body = """
      {"customerId":"11111111-1111-1111-1111-111111111111"}
    """;

        mvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
