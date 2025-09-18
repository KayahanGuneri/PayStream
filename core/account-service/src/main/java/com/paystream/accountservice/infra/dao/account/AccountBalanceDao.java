package com.paystream.accountservice.infra.dao.account;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * DAO for account_balances table in minor units.
 * Idempotent update: apply only if new offset is strictly higher.
 */
@Repository
public class AccountBalanceDao {

    private final JdbcTemplate jdbc;

    public AccountBalanceDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Inserts an initial balance row for the given account. */
    public void insertInitial(UUID accountId) {
        final String sql = """
            INSERT INTO account_balances (account_id, balance_minor, as_of_ledger_offset, updated_at)
            VALUES (?, 0, NULL, now())
        """;
        jdbc.update(sql, accountId);
    }

    /**
     * Apply delta in minor units if the incoming ledgerOffset is newer.
     * @return rows updated (0 if skipped → idempotent)
     */
    public int applyDelta(UUID accountId, long deltaMinor, long ledgerOffset) {
        final String sql = """
            UPDATE account_balances
               SET balance_minor       = balance_minor + ?,
                   as_of_ledger_offset = ?,
                   updated_at          = now()
             WHERE account_id = ?
               AND (as_of_ledger_offset IS NULL OR as_of_ledger_offset < ?)
        """;
        return jdbc.update(sql, deltaMinor, ledgerOffset, accountId, ledgerOffset);
    }
}
