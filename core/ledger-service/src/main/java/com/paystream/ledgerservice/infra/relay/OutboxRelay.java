package com.paystream.ledgerservice.infra.relay;

import com.paystream.ledgerservice.domain.OutboxRecord;
import com.paystream.ledgerservice.infra.repo.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxRepository outboxRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // Periodically scans unpublished outbox rows and publishes them to Kafka
    @Scheduled(fixedDelay = 250)
    public void pump() {
        List<OutboxRecord> batch = outboxRepo.fetchUnpublishedBatch(100);
        for (OutboxRecord rec : batch) {
            // Use key=accountId to ensure ordering per account (partition key)
            kafkaTemplate.send("ledger.entry.appended", rec.getKeyAccountId().toString(), rec.getPayload());
            outboxRepo.markPublished(rec.getId());
        }
    }
}
