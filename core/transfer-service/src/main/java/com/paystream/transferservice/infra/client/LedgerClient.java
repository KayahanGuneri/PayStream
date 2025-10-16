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
 * Minimal HTTP client to communicate with ledger-service.
 * - Uses RestClient.Builder injected by Spring.
 * - Retries transient errors (5xx / 408) up to 3 times.
 * - Throws domain exception on business failure (422).
 */
@Component
@RequiredArgsConstructor
public class LedgerClient {

    private final RestClient.Builder restBuilder;

    // Default base URL points to Docker container or Eureka service
    @Value("${ledger.base-url:http://ledger-service:9100}")
    private String ledgerBaseUrl;

    /**
     * Appends a double-entry transaction to the ledger.
     * @return true if operation succeeded (HTTP 2xx)
     */
    public boolean appendDoubleEntry(UUID txId,
                                     UUID source,
                                     UUID dest,
                                     String currency,
                                     long amountMinor) {

        // Build JSON body
        Map<String, Object> body = Map.of(
                "txId", txId.toString(),
                "entries", List.of(
                        Map.of("accountId", source.toString(), "currency", currency, "amountMinor", -amountMinor),
                        Map.of("accountId", dest.toString(), "currency", currency, "amountMinor", amountMinor)
                )
        );

        RestClient http = restBuilder.baseUrl(ledgerBaseUrl).build();

        int attempts = 0;
        while (true) {
            try {
                http.post()
                        .uri("/v1/ledger/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
                return true;
            } catch (RestClientResponseException ex) {
                int status = ex.getRawStatusCode();

                // 422: Business failure
                if (status == 422) {
                    throw new InsufficientFundsException("Ledger rejected transaction: " + ex.getResponseBodyAsString());
                }

                // Do not retry on 4xx except 408
                if (status >= 400 && status < 500 && status != 408) {
                    throw ex;
                }

                // Retry up to 3 times on 5xx / 408
                if (++attempts >= 3) throw ex;
                try {
                    Thread.sleep(200L * attempts);
                } catch (InterruptedException ignored) { }
            }
        }
    }
}
