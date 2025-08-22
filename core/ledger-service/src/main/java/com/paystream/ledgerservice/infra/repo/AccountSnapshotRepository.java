package com.paystream.ledgerservice.infra.repo;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountSnapshotRepository {

    private final JdbcTemplate jdbc;

    /**
     * Idempotent: Aynı/eskiden gelen offset'ler etkisiz kalır.
     */
    public void applyDelta(UUID accountId, String currency, long deltaMinor, long offset) {
        final String sql = """
          INSERT INTO account_snapshots (account_id, currency, balance_minor, last_offset)
          VALUES (?, ?, ?, ?)
          ON CONFLICT (account_id, currency) DO UPDATE
            SET balance_minor = CASE
                                   WHEN EXCLUDED.last_offset > account_snapshots.last_offset
                                     THEN account_snapshots.balance_minor + EXCLUDED.balance_minor
                                   ELSE account_snapshots.balance_minor
                                END,
                last_offset   = GREATEST(account_snapshots.last_offset, EXCLUDED.last_offset),
                updated_at    = CASE
                                   WHEN EXCLUDED.last_offset > account_snapshots.last_offset
                                     THEN now()
                                   ELSE account_snapshots.updated_at
                                END
        """;
        jdbc.update(sql, accountId, currency, deltaMinor, offset);
    }
}
