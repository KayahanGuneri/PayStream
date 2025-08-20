package com.paystream.accountservice.infra.dao.account;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DAO for account_balances table.
 * - insertInitial: creates the starting row (0 balance, null offset)
 * - updateBalanceWithOffset: idempotent update guarded by a monotonic ledger offset
 */
@Repository
public class AccountBalanceDao {

    private final JdbcTemplate jdbc;

    public AccountBalanceDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Inserts an initial balance row for the given account. */
    public void insertInitial(UUID accountId) {
        final String sql =
                "INSERT INTO account_balances (account_id, current_balance, as_of_ledger_offset, updated_at) " +
                        "VALUES (?, 0, NULL, now())";
        jdbc.update(sql, accountId);
    }

    /**
     * Idempotent balance update using a monotonic ledger offset.
     * Only updates when previous offset is NULL or equals expectedPrevOffset.
     *
     * @return number of rows updated (0 if skipped)
     */
    public int updateBalanceWithOffset(UUID accountId, BigDecimal delta, long newOffset, Long expectedPrevOffset) {
        final String sql =
                "UPDATE account_balances " +
                        "   SET current_balance = current_balance + ?, " +
                        "       as_of_ledger_offset = ?, " +
                        "       updated_at = now() " +
                        " WHERE account_id = ? " +
                        "   AND (as_of_ledger_offset IS NULL OR as_of_ledger_offset = ?)";
        return jdbc.update(sql, delta, newOffset, accountId, expectedPrevOffset);
    }
}
