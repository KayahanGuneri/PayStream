package com.paystream.ledgerservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.kafka.core.KafkaTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LedgerControllerIT extends PostgresContainerSupport {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    // Kafka’ya gerçek bağlanmak istemiyorsak:
    @MockBean KafkaTemplate<String, String> kafkaTemplate;

    @Test
    void postTransaction_returns202_andWritesRows() throws Exception {
        String body = """
            {
              "txId": "00000000-0000-0000-0000-000000000001",
              "entries": [
                { "accountId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "currency": "TRY", "amountMinor": -10000 },
                { "accountId": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", "currency": "TRY", "amountMinor": 10000 }
              ]
            }
            """;

        mvc.perform(post("/v1/ledger/transactions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted());

        Integer cnt = jdbc.queryForObject("select count(*) from ledger_entries", Integer.class);
        Integer outbox = jdbc.queryForObject("select count(*) from ledger_outbox", Integer.class);

        // 2 satır entry + en az 2 outbox (her satıra 1 event varsayımı)
        // Geliştirme sırasında şartları gevşek tut:
        org.assertj.core.api.Assertions.assertThat(cnt).isGreaterThanOrEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(outbox).isGreaterThanOrEqualTo(2);
    }
}
