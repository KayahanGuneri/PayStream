package com.paystream.ledgerservice.infra.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paystream.ledgerservice.infra.repo.AccountSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ledger.snapshot.consumer.enabled", havingValue = "true", matchIfMissing = true)
public class LedgerSnapshotConsumer {

    private final ObjectMapper om;
    private final AccountSnapshotRepository snapshots;

    record Event(UUID accountId, String currency, long amountMinor, long ledgerOffset) {}

    @KafkaListener(
            topics = "ledger.entry.appended",
            groupId = "ledger-snapshot-consumer",
            containerFactory = "ledgerKafkaListenerContainerFactory" // aşağıdaki config ile eşleşecek
    )
    public void onMessage(ConsumerRecord<String, String> rec, Acknowledgment ack) throws Exception {
        try {
            Event e = om.readValue(rec.value(), Event.class);
            snapshots.applyDelta(e.accountId(), e.currency(), e.amountMinor(), e.ledgerOffset());
            ack.acknowledge(); // idempotent olduğumuz için güvenle ACK
        } catch (Exception ex) {
            log.error("Snapshot apply failed, will be retried. offset={}, payload={}", rec.offset(), rec.value(), ex);
            // ACK YOK → framework tekrar deneyecek / DLT politikasına bırakılabilir
            throw ex; // retry mekanizmasına bırak
        }
    }
}
