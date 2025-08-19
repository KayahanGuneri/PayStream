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
        // Use Testcontainers DB
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        // Disable Eureka during tests
        r.add("eureka.client.enabled", () -> "false");
        // Ensure migrations run
        r.add("spring.flyway.enabled", () -> "true");
        // Force servlet stack
        r.add("spring.main.web-application-type", () -> "servlet");
    }

    @Autowired MockMvc mvc;

    @Test
    @DisplayName("GET /accounts/{id}/balance -> 400 when id is malformed UUID")
    void getBalance_malformedUuid_shouldReturn400() throws Exception {
        // When the path variable is not a valid UUID, Spring fails conversion -> 400 Bad Request
        mvc.perform(get("/accounts/{id}/balance", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Nested
    class InvalidCurrencyOnCreate {

        @Test
        @DisplayName("POST /accounts -> 400 when currency is too short")
        void currencyTooShort() throws Exception {
            // currency must be exactly 3 letters (e.g., TRY). Here: "TR"
            String body = """
                {"customerId":"11111111-1111-1111-1111-111111111111","currency":"TR"}
            """;
            mvc.perform(post("/accounts").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /accounts -> 400 when currency is too long")
        void currencyTooLong() throws Exception {
            // "TRYY" is 4 chars -> invalid
            String body = """
                {"customerId":"11111111-1111-1111-1111-111111111111","currency":"TRYY"}
            """;
            mvc.perform(post("/accounts").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /accounts -> 400 when currency is lowercase")
        void currencyLowercase() throws Exception {
            // Lowercase "try" is invalid if you validate with @Pattern(regexp = "^[A-Z]{3}$")
            String body = """
                {"customerId":"11111111-1111-1111-1111-111111111111","currency":"try"}
            """;
            mvc.perform(post("/accounts").contentType("application/json").content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
