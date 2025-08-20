package com.paystream.accountservice.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class EdgeCasesHttpIT {

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
        r.add("eureka.client.enabled", () -> "false");
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.main.web-application-type", () -> "servlet");
    }

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("GET /v1/accounts/{id} -> 400 when id is malformed UUID")
    void getAccount_malformedUuid_shouldReturn400() throws Exception {
        mvc.perform(get("/v1/accounts/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Nested
    class InvalidCurrencyOnCreate {

        @Test
        @DisplayName("POST /v1/customers/{customerId}/accounts -> 400 when currency is too short")
        void currencyTooShort() throws Exception {
            String body = """
                {"currency":"TR"}
            """;
            mvc.perform(post("/v1/customers/{customerId}/accounts", "11111111-1111-1111-1111-111111111111")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /v1/customers/{customerId}/accounts -> 400 when currency is too long")
        void currencyTooLong() throws Exception {
            String body = """
                {"currency":"TRYY"}
            """;
            mvc.perform(post("/v1/customers/{customerId}/accounts", "11111111-1111-1111-1111-111111111111")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /v1/customers/{customerId}/accounts -> 400 when currency is lowercase")
        void currencyLowercase() throws Exception {
            String body = """
                {"currency":"try"}
            """;
            mvc.perform(post("/v1/customers/{customerId}/accounts", "11111111-1111-1111-1111-111111111111")
                            .contentType("application/json")
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
