package com.paystream.ledgerservice.integration;

import com.paystream.ledgerservice.domain.LedgerEntry;
import com.paystream.ledgerservice.domain.OutboxRecord;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository;
import com.paystream.ledgerservice.infra.repo.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LedgerRepositoryIT extends PostgresContainerSupport {

    @Autowired LedgerEntryRepository ledgerRepo;
    @Autowired OutboxRepository outboxRepo;

    @Test
    void insert_ledgerEntry_assignsOffsetAndTimestamps() {
        LedgerEntry e = LedgerEntry.builder()
                .entryId(UUID.randomUUID())
                .txId(UUID.randomUUID())
                .txSeq(0)
                .accountId(UUID.randomUUID())
                .currency("TRY")
                .amountMinor(-1000)
                .build();

        ledgerRepo.insert(e);

        assertThat(e.getLedgerOffset()).isNotNull().isGreaterThan(0L);
        assertThat(e.getCreatedAt()).isNotNull();
    }

    @Test
    void outbox_insert_and_markPublished() {
        OutboxRecord rec = OutboxRecord.builder()
                .id(UUID.randomUUID())
                .eventType("ledger.entry.appended")
                .keyAccountId(UUID.randomUUID())
                .payload("{\"x\":1}")
                .build();
        outboxRepo.insert(rec);
        assertThat(rec.getCreatedAt()).isNotNull();
        assertThat(rec.getPublishedAt()).isNull();

        outboxRepo.markPublished(rec.getId());

    }
}
