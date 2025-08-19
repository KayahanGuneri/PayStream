package com.paystream.accountservice.infra.dao;

import com.paystream.accountservice.domain.Account;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountDao {
    private final JdbcTemplate jdbc;

    public AccountDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void insert(Account a) {
        String sql = """
            INSERT INTO accounts(id, customer_id, currency, status, version, created_at, updated_at)
            VALUES (?,?,?,?,?, ?,?)
        """;
        Instant now = Instant.now();
        jdbc.update(sql,
                a.id(), a.customerId(), a.currency(), a.status(), a.version(),
                Timestamp.from(now), Timestamp.from(now));
    }

    public Optional<Account> findById(UUID id) {
        var list = jdbc.query("SELECT * FROM accounts WHERE id=?", RowMappers.ACCOUNT, id);
        return list.stream().findFirst();
    }


    public int updateStatusWithOptimisticLock(UUID id, String newStatus, long expectedVersion) {
        String sql = """
            UPDATE accounts
               SET status=?, version=version+1, updated_at=now()
             WHERE id=? AND version=?
        """;
        return jdbc.update(sql, newStatus, id, expectedVersion);
    }
}
