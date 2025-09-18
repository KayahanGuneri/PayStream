// Minimal internal client to call Ledger Service via HTTP.
package com.paystream.transferservice.infra.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class LedgerClient {

    private final RestClient http;

    public LedgerClient(RestClient.Builder lbBuilder,
                        @Value("${ledger.base-url}") String ledgerBaseUrl) {
        this.http = lbBuilder.baseUrl(ledgerBaseUrl).build();
    }

    public boolean appendDoubleEntry(UUID ledgerTxId, UUID source, UUID dest, String currency, long amountMinor) {
        var body = Map.of(
                "txId", ledgerTxId.toString(),
                "entries", new Object[]{
                        Map.of("accountId", source.toString(), "currency", currency, "amountMinor", -amountMinor),
                        Map.of("accountId", dest.toString(),   "currency", currency, "amountMinor",  amountMinor)
                }
        );

        http.post()
                .uri("/v1/ledger/transactions")   // LedgerController: @RequestMapping("/v1/ledger"), POST "/transactions"
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();

        return true;
    }
}
