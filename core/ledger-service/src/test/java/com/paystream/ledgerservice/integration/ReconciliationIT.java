package com.paystream.ledgerservice.integration;

import com.paystream.ledgerservice.config.TestKafkaConfig;
import com.paystream.ledgerservice.domain.LedgerEntry;
import com.paystream.ledgerservice.infra.repo.AccountSnapshotRepository;
import com.paystream.ledgerservice.infra.repo.LedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestKafkaConfig.class)
class ReconciliationIT extends PostgresContainerSupport {

    @BeforeEach
    void cleanDb() {
        // Sıra önemli değil, FK yoksa TRUNCATE yeterli
        jdbc.update("TRUNCATE TABLE account_snapshots");
        jdbc.update("TRUNCATE TABLE ledger_entries");
    }


    @Autowired LedgerEntryRepository ledgerRepo;
    @Autowired AccountSnapshotRepository snapshots;
    @Autowired JdbcTemplate jdbc;

    @Test
    void ledger_sum_equals_snapshot_and_idempotent() {
        UUID accA = UUID.randomUUID();
        UUID accB = UUID.randomUUID();
        String TRY = "TRY";

        // Tx1: A -1000, B +1000
        long o1 = insert(accA, TRY, -1000, 0);
        snapshots.applyDelta(accA, TRY, -1000, o1);
        long o2 = insert(accB, TRY, +1000, 1);
        snapshots.applyDelta(accB, TRY, +1000, o2);

        // Tx2: A +500, B -500
        long o3 = insert(accA, TRY, +500, 0);
        snapshots.applyDelta(accA, TRY, +500, o3);
        long o4 = insert(accB, TRY, -500, 1);
        snapshots.applyDelta(accB, TRY, -500, o4);

        // 1) Ledger toplamları
        Map<Key, Long> ledgerSums = jdbc.query("""
            SELECT account_id, currency, SUM(amount_minor) AS s
            FROM ledger_entries
            GROUP BY account_id, currency
        """, rs -> {
            Map<Key, Long> m = new HashMap<>();
            while (rs.next()) {
                Key k = new Key((UUID)rs.getObject("account_id"), rs.getString("currency"));
                m.put(k, rs.getLong("s"));
            }
            return m;
        });

        // 2) Snapshot bakiyeleri
        Map<Key, Long> snapshotBalances = jdbc.query("""
            SELECT account_id, currency, balance_minor
            FROM account_snapshots
        """, rs -> {
            Map<Key, Long> m = new HashMap<>();
            while (rs.next()) {
                Key k = new Key((UUID)rs.getObject("account_id"), rs.getString("currency"));
                m.put(k, rs.getLong("balance_minor"));
            }
            return m;
        });

        // 3) Karşılaştırma (her key için eşit olmalı)
        assertThat(snapshotBalances).containsAllEntriesOf(ledgerSums);

        // 4) Idempotency: aynı event'i tekrar uygula -> değişmemeli
        long before = snapshotBalances.get(new Key(accA, TRY));
        snapshots.applyDelta(accA, TRY, -1000, o1); // duplicate (aynı offset)
        long after = jdbc.queryForObject("""
            SELECT balance_minor FROM account_snapshots
            WHERE account_id=? AND currency=?
        """, Long.class, accA, TRY);
        assertThat(after).isEqualTo(before);
    }

    @Test
    void out_of_order_event_is_noop() {
        UUID acc = UUID.randomUUID();
        String TRY = "TRY";

        long o1 = insert(acc, TRY, +100, 0); // offset = o1
        long o2 = insert(acc, TRY, +1000, 1); // offset = o2
        long o3 = insert(acc, TRY, +50, 2);   // offset = o3  (o1 < o2 < o3)

        // Snapshot uygulama sırasını karıştır: o1 → o3 → o2
        snapshots.applyDelta(acc, TRY, +100,  o1); // balance: 100,  offset=o1
        snapshots.applyDelta(acc, TRY, +50,   o3); // balance: 150,  offset=o3
        snapshots.applyDelta(acc, TRY, +1000, o2); // o2 < o3 → NO-OP

        Long bal = jdbc.queryForObject("""
        SELECT balance_minor FROM account_snapshots
        WHERE account_id=? AND currency=?""", Long.class, acc, TRY);
        Long off = jdbc.queryForObject("""
        SELECT as_of_ledger_offset FROM account_snapshots
        WHERE account_id=? AND currency=?""", Long.class, acc, TRY);

        assertThat(bal).isEqualTo(150L); // 100 + 50; o2 NO-OP
        assertThat(off).isEqualTo(o3);
    }



    private long insert(UUID accountId, String currency, long amountMinor, int txSeq) {
        LedgerEntry e = LedgerEntry.builder()
                .entryId(UUID.randomUUID())
                .txId(UUID.randomUUID())
                .txSeq(txSeq)
                .accountId(accountId)
                .currency(currency)
                .amountMinor(amountMinor)
                .build();
        return ledgerRepo.insert(e);
    }

    private record Key(UUID accountId, String currency) { }
}
