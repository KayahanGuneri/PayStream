package com.paystream.ledgerservice.infra.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.ledgerservice.infra.repo.AccountSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ledger.snapshot.consumer.enabled", havingValue = "true")
public class LedgerSnapshotConsumer {

    private final ObjectMapper om;
    private final AccountSnapshotRepository snapshots;

    @KafkaListener(topics = "ledger.entry.appended", groupId = "ledger-snapshot-consumer")
    public void onMessage(String payload) throws Exception {
        Event e = om.readValue(payload, Event.class);
        snapshots.applyDelta(e.accountId, e.currency, e.amountMinor, e.ledgerOffset);
    }

    record Event(UUID accountId, String currency, long amountMinor, long ledgerOffset) {}
}
