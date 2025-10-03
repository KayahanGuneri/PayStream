package com.paystream.transferservice.infra.client;

import com.paystream.transferservice.domain.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Very small HTTP client to talk to ledger-service.
 * - Uses the shared plain RestClient.Builder
 * - Base URL comes from property: ledger.base-url
 * - Simple retry on transient errors (5xx / 408)
 */
@Component
@RequiredArgsConstructor
public class LedgerClient {

    private final RestClient.Builder builder;

    // Default points to docker container name + port
    @Value("${ledger.base-url:http://ledger-service:9100}")
    private String ledgerBaseUrl;

    public boolean appendDoubleEntry(UUID txId,
                                     UUID source,
                                     UUID dest,
                                     String currency,
                                     long amountMinor) {
        // Build minimal JSON body
        Map<String, Object> body = Map.of(
                "txId", txId.toString(),
                "entries", List.of(
                        Map.of("accountId", source.toString(), "currency", currency, "amountMinor", -amountMinor),
                        Map.of("accountId", dest.toString(),   "currency", currency, "amountMinor",  amountMinor)
                )
        );

        // Create a client with the base URL
        RestClient http = builder.baseUrl(ledgerBaseUrl).build();

        int attempts = 0;
        while (true) {
            try {
                http.post()
                        .uri("/v1/ledger/transactions")   // ledger endpoint
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();               // expect 2xx with no body
                return true;
            } catch (RestClientResponseException ex) {
                int status = ex.getRawStatusCode();

                // Business error from ledger (e.g., not enough funds)
                if (status == 422) {
                    throw new InsufficientFundsException("Ledger rejected: " + ex.getResponseBodyAsString());
                }

                // For 4xx (except 408), do not retry
                if (status >= 400 && status < 500 && status != 408) {
                    throw ex;
                }

                // Retry on 5xx and 408, up to 3 attempts
                if (++attempts >= 3) {
                    throw ex;
                }
                try { Thread.sleep(200L * attempts); } catch (InterruptedException ignored) { }
            }
        }
    }
}
