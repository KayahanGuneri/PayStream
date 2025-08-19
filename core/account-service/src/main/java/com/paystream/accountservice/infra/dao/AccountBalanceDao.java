package com.paystream.accountservice.infra.dao;

import com.paystream.accountservice.domain.AccountBalance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountBalanceDao {
    private final JdbcTemplate jdbc;

    public AccountBalanceDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void insertInitial(UUID accountId) {
        String sql = """
            INSERT INTO account_balances(account_id, current_balance, as_of_ledger_offset, updated_at)
            VALUES (?,?,NULL,?)
        """;
        jdbc.update(sql, accountId, BigDecimal.ZERO, Timestamp.from(Instant.now()));
    }

    public Optional<AccountBalance> findById(UUID accountId) {
        var list = jdbc.query("SELECT * FROM account_balances WHERE account_id=?",
                RowMappers.ACCOUNT_BALANCE, accountId);
        return list.stream().findFirst();
    }

    // İleride ledger eventlerine göre güncelleme (idempotent offset kontrolü ile)
    public int updateBalanceWithOffset(UUID accountId, BigDecimal delta, long newOffset, Long expectedPrevOffset) {
        String sql = """
            UPDATE account_balances
               SET current_balance = current_balance + ?,
                   as_of_ledger_offset = ?,
                   updated_at = now()
             WHERE account_id = ?
               AND (as_of_ledger_offset IS NULL OR as_of_ledger_offset = ?)
        """;
        return jdbc.update(sql, delta, newOffset, accountId, expectedPrevOffset);
    }
}
