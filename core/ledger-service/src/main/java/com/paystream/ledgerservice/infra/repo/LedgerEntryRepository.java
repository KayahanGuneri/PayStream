package com.paystream.ledgerservice.infra.repo;

import com.paystream.ledgerservice.domain.LedgerEntry;
import com.paystream.ledgerservice.infra.mapper.LedgerEntryRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository // Persistence boundary: contains SQL only
@RequiredArgsConstructor // Injects jdbc and rowMapper via constructor
public class LedgerEntryRepository {

    private final JdbcTemplate jdbc;                 // low-level DB access
    private final LedgerEntryRowMapper rowMapper;    // maps DB rows to domain

    // Inserts a ledger line, assigns global offset from sequence, returns the offset
    public long insert(LedgerEntry e) {
        // Using RETURNING to fetch the assigned ledger_offset in one round-trip
        final String sql = """
            INSERT INTO ledger_entries(
                entry_id, tx_id, tx_seq, account_id, currency, amount_minor, created_at, ledger_offset
            ) VALUES (?, ?, ?, ?, ?, ?, now(), nextval('ledger_offset_seq'))
            RETURNING ledger_offset
        """;

        Long offset = jdbc.queryForObject(
                sql, Long.class,
                e.getEntryId(), e.getTxId(), e.getTxSeq(), e.getAccountId(),
                e.getCurrency(), e.getAmountMinor()
        );

        // Update in-memory object for event payload composition
        e.setLedgerOffset(offset);
        e.setCreatedAt(OffsetDateTime.now());
        return offset;
    }

}
