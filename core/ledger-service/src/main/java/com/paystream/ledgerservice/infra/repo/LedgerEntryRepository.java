package com.paystream.ledgerservice.infra.repo;

import com.paystream.ledgerservice.domain.LedgerEntry;
import com.paystream.ledgerservice.infra.mapper.LedgerEntryRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LedgerEntryRepository {

    private final JdbcTemplate jdbc;
    private final LedgerEntryRowMapper rowMapper;

    public static record InsertResult(long offset, boolean inserted, OffsetDateTime createdAt) {}

    public InsertResult upsert(LedgerEntry e) {
        final String sql = """
            INSERT INTO ledger_entries(
                entry_id, tx_id, tx_seq, account_id, currency, amount_minor, created_at, ledger_offset
            ) VALUES (?, ?, ?, ?, ?, ?, now(), nextval('ledger_offset_seq'))
            ON CONFLICT (tx_id, tx_seq) DO NOTHING
            RETURNING ledger_offset, created_at
        """;

        var list = jdbc.query(sql,
                rs -> {
                    if (!rs.next()) return List.<InsertResult>of();
                    var created = rs.getObject("created_at", OffsetDateTime.class);
                    return List.of(new InsertResult(rs.getLong("ledger_offset"), true, created));
                },
                e.getEntryId(), e.getTxId(), e.getTxSeq(), e.getAccountId(), e.getCurrency(), e.getAmountMinor()
        );

        if (!list.isEmpty()) {
            var r = list.get(0);
            e.setLedgerOffset(r.offset());
            e.setCreatedAt(r.createdAt());
            return r;
        }

        // Kayıt zaten var → mevcut offset/created_at değerini çek
        var existing = jdbc.query(
                "SELECT ledger_offset, created_at FROM ledger_entries WHERE tx_id=? AND tx_seq=?",
                rs -> rs.next()
                        ? new InsertResult(rs.getLong("ledger_offset"), false, rs.getObject("created_at", OffsetDateTime.class))
                        : null,
                e.getTxId(), e.getTxSeq()
        );

        if (existing == null)
            throw new IllegalStateException("Ledger entry upsert: existing row not found though conflict happened");

        e.setLedgerOffset(existing.offset());
        e.setCreatedAt(existing.createdAt());
        return existing;
    }
}