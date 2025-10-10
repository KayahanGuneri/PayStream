
// Minimal internal client to call Ledger Service via HTTP with retry and error mapping.
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

 * Talks to ledger-service using Spring's RestClient.
 * - Builds a client per call using the shared LoadBalanced builder
 * - Uses baseUrl from property (defaults to Eureka id http://ledger-service)
 * - Implements a simple retry for transient failures (idempotent operation)
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
    private final RestClient.Builder lbBuilder;

    // Default to Eureka service id; can be overridden in application-*.yml
    @Value("${ledger.base-url:http://ledger-service}")
    private String ledgerBaseUrl;

    /**
     * Appends a double-entry transaction to the ledger.
     * @return true when HTTP 2xx is received
     * @throws InsufficientFundsException when ledger returns 422 (business failure)
     * @throws RestClientResponseException for non-retriable 4xx/5xx after retries
     */
    public boolean appendDoubleEntry(UUID ledgerTxId,
                                     UUID source,
                                     UUID dest,
                                     String currency,
                                     long amountMinor) {

        // Build body as a plain Map (keeps the wire contract simple)
        Map<String, Object> body = Map.of(
                "txId", ledgerTxId.toString(),
                "entries", new Object[]{
                        Map.of("accountId", source.toString(), "currency", currency, "amountMinor", -amountMinor),
                        Map.of("accountId", dest.toString(),   "currency", currency, "amountMinor",  amountMinor)
                )
        );

        // Create a client with the base URL
        RestClient http = builder.baseUrl(ledgerBaseUrl).build();


        // Build RestClient per call: thread-safe and allows per-call baseUrl
        RestClient http = lbBuilder.baseUrl(ledgerBaseUrl).build();

        // Simple retry for transient issues (network hiccups, 5xx).
        // NOTE: For production-grade, prefer a proper retry/circuit-breaker library.
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

                        .uri("/v1/ledger/transactions") // LedgerController: POST /transactions
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();            // 2xx → no body expected
                return true;                        // success
            } catch (RestClientResponseException ex) {
                int status = ex.getRawStatusCode();

                // Map business failure (insufficient funds) explicitly to domain exception
                if (status == 422) {
                    throw new InsufficientFundsException("Ledger rejected transaction: " + ex.getResponseBodyAsString());
                }

                // Do not retry on 4xx (client errors) except transient 408
                if (status >= 400 && status < 500 && status != 408) {
                    throw ex;
                }

                // Retry on 5xx and 408 (request timeout)
                if (++attempts >= 3) throw ex;     // give up after 3 attempts

                // Exponential-ish backoff with a small sleep (no scheduler needed)
                try { Thread.sleep(200L * attempts); } catch (InterruptedException ignored) { /* no-op */ }
            }
        }
    }
}
