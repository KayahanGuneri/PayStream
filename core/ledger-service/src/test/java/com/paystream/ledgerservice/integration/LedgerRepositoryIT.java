package com.paystream.ledgerservice.integration;

import com.paystream.ledgerservice.config.TestKafkaConfig;
import com.paystream.ledgerservice.domain.LedgerEntry;
import com.paystream.ledgerservice.domain.OutboxRecord;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository;
import com.paystream.ledgerservice.infra.repo.OutboxRepository;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository.InsertResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)
class LedgerRepositoryIT extends PostgresContainerSupport {

    @Autowired LedgerEntryRepository ledgerRepo;
    @Autowired OutboxRepository outboxRepo;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void cleanDb() {
        // İzolasyon: her testten önce veriyi sıfırla
        jdbc.update("TRUNCATE TABLE ledger_entries");
        // Outbox tablo adı projede genelde 'outbox_events'
        jdbc.update("TRUNCATE TABLE outbox_events");
    }

    @Test
    void insert_ledgerEntry_assignsOffsetAndTimestamps() {
        // given
        UUID entryId = UUID.randomUUID();
        LedgerEntry e = LedgerEntry.builder()
                .entryId(entryId)
                .txId(UUID.randomUUID())
                .txSeq(0)
                .accountId(UUID.randomUUID())
                .currency("TRY")
                .amountMinor(-1000)
                .build();

        // when
        InsertResult res = ledgerRepo.upsert(e);

        // then — repository nesneyi yerinde güncellemek zorunda değil;
        // offset/timestamp’i DB’den doğrula
        Long offset = jdbc.queryForObject("""
            SELECT ledger_offset FROM ledger_entries WHERE entry_id=?
        """, Long.class, entryId);
        assertThat(offset).isNotNull().isGreaterThan(0L);

        // created_at dolu mu?
        Boolean hasCreatedAt = jdbc.queryForObject("""
            SELECT created_at IS NOT NULL FROM ledger_entries WHERE entry_id=?
        """, Boolean.class, entryId);
        assertThat(hasCreatedAt).isTrue();

        // (Opsiyonel) Repository'nin döndürdüğü InsertResult da tutarlı mı?
        // InsertResult içinde accessor adı projene göre değişebilir (offset(), getOffset(), asOfOffset() ...).
        // Aşağıdakini kendi tipine göre uyarlayabilirsin:
        // assertThat(res.offset()).isEqualTo(offset);
    }

    @Test
    void outbox_insert_and_markPublished() {
        // given
        UUID id = UUID.randomUUID();
        UUID keyAcc = UUID.randomUUID();

        OutboxRecord rec = OutboxRecord.builder()
                .id(id)
                .eventType("ledger.entry.appended")
                .keyAccountId(keyAcc)
                .payload("{\"x\":1}")
                .build();

        // when
        outboxRepo.insert(rec);

        // then — created_at dolu mu?
        Boolean hasCreatedAt = jdbc.queryForObject("""
            SELECT created_at IS NOT NULL FROM outbox_events WHERE id=?
        """, Boolean.class, id);
        assertThat(hasCreatedAt).isTrue();

        // publish edilmeden önce published_at NULL olmalı
        Boolean publishedBefore = jdbc.queryForObject("""
            SELECT published_at IS NOT NULL FROM outbox_events WHERE id=?
        """, Boolean.class, id);
        assertThat(publishedBefore).isFalse();

        // when — publish et
        outboxRepo.markPublished(id);

        // then — artık published_at dolu olmalı
        Boolean publishedAfter = jdbc.queryForObject("""
            SELECT published_at IS NOT NULL FROM outbox_events WHERE id=?
        """, Boolean.class, id);
        assertThat(publishedAfter).isTrue();
    }
}
