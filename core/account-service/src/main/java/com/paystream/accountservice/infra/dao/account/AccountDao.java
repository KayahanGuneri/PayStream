package com.paystream.accountservice.infra.dao.account;

import com.paystream.accountservice.domain.account.Account;
import com.paystream.accountservice.infra.dao.mapper.RowMappers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountDao {

    private final JdbcTemplate jdbc;

    public AccountDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Account a) {
        String sql =
                "INSERT INTO accounts (id, customer_id, currency, status, version, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, 0, now(), now())";

        jdbc.update(
                sql,
                a.id(),
                a.customerId(),
                a.currency(),
                a.status()
        );
    }

    public Optional<Account> findById(UUID id) {
        String sql =
                "SELECT id, customer_id, currency, status, version, created_at, updated_at " +
                        "FROM accounts WHERE id = ?";

        return jdbc
                .query(sql, RowMappers.ACCOUNT, id)
                .stream()
                .findFirst();
    }

    /**
     * Belirli bir customerId'ye ait TÜM hesapları döndürür.
     * Kayıt yoksa null değil, boş liste döner.
     */
    public List<Account> findByCustomerId(UUID customerId) {
        String sql =
                "SELECT id, customer_id, currency, status, version, created_at, updated_at " +
                        "FROM accounts WHERE customer_id = ?";

        return jdbc.query(sql, RowMappers.ACCOUNT, customerId);
    }
}
