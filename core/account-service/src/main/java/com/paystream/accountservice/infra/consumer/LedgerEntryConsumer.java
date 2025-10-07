package com.paystream.accountservice.infra.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paystream.accountservice.infra.dao.account.AccountBalanceDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Consumes ledger.entry.appended and applies balance deltas idempotently. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerEntryConsumer {

    private final ObjectMapper om;
    private final AccountBalanceDao balances;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Event(
            UUID accountId,
            String currency,   // not used here but useful for validation later
            long amountMinor,  // signed
            long ledgerOffset  // global, monotonically increasing
    ) {}

    @KafkaListener(
            topics = "ledger.entry.appended",
            groupId = "account-snapshot-consumer"
    )
    public void onMessage(ConsumerRecord<String, String> rec) throws Exception {
        Event e = om.readValue(rec.value(), Event.class);

        // Apply idempotently: only if offset is newer than snapshot
        int updated = balances.applyDelta(e.accountId(), e.amountMinor(), e.ledgerOffset());
        if (updated == 1) {
            log.info("Snapshot updated acc={} Δ={} offset={}", e.accountId(), e.amountMinor(), e.ledgerOffset());
        } else {
            log.debug("Skipped (old/duplicate) acc={} Δ={} offset={}", e.accountId(), e.amountMinor(), e.ledgerOffset());
        }
    }
}
