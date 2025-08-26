// Minimal internal client to call Ledger Service via HTTP.
package com.paystream.transferservice.infra.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component // generic DI bean
public class LedgerClient {

    private final RestClient http;

    // base URL is configurable; Docker internal DNS resolves service names on the same network
    public LedgerClient(RestClient.Builder lbBuilder) {
        // Eureka service-id (app name of ledger service)
        this.http = lbBuilder.baseUrl("http://ledger-service").build();
    }

    // Calls an internal endpoint that appends a double-entry pair to the ledger.
    public boolean appendDoubleEntry(UUID ledgerTxId, UUID source, UUID dest, String currency, long amountMinor) {
        var body = Map.of(
                "txId", ledgerTxId.toString(),
                "entries", new Object[]{
                        Map.of("accountId", source.toString(), "currency", currency, "amountMinor", -amountMinor),
                        Map.of("accountId", dest.toString(),   "currency", currency, "amountMinor",  amountMinor)
                }
        );

        var resp = http.post()
                .uri("/internal/ledger/append")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        return resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode() == HttpStatus.NO_CONTENT;
    }

package com.paystream.transferservice.infra.client;

public class LedgerClient {

}
