package com.paystream.ledgerservice.infra.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.ledgerservice.infra.repo.AccountSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "ledger.snapshot.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class LedgerEntryConsumer {

    private final ObjectMapper om = new ObjectMapper();
    private final AccountSnapshotRepository snapshots;

    // topic: ledger.entry.appended, key: accountId
    @KafkaListener(topics = "ledger.entry.appended", groupId = "ledger-snapshot-consumer")
    public void onMessage(ConsumerRecord<String, String> rec) throws Exception {
        String payload = rec.value();
        JsonNode n = om.readTree(payload);

        UUID accountId = UUID.fromString(n.get("accountId").asText());
        String currency = n.get("currency").asText();
        long amountMinor = n.get("amountMinor").asLong();
        long offset = n.get("ledgerOffset").asLong();

        snapshots.applyDelta(accountId, currency, amountMinor, offset);
    }
}
