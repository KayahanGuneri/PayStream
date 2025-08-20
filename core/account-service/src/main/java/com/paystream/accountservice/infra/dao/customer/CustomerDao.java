package com.paystream.accountservice.infra.dao.customer;

import com.paystream.accountservice.domain.customer.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO for the customers table using JdbcTemplate.
 */
@Repository
public class CustomerDao {

    private final JdbcTemplate jdbc;

    public CustomerDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(Customer c) {
        String sql = """
            INSERT INTO customers (id, name, email, password_hash, created_at, updated_at)
            VALUES (?, ?, ?, ?, NOW(), NOW())
            """;
        jdbc.update(sql, c.id(), c.name(), c.email(), c.passwordHash());
    }

    public Optional<Customer> findById(UUID id) {
        String sql = "SELECT id, name, email, password_hash, created_at, updated_at FROM customers WHERE id = ?";
        return jdbc.query(sql, rs -> rs.next() ? Optional.of(map(rs)) : Optional.empty(), id);
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM customers WHERE email = ? LIMIT 1";
        return Boolean.TRUE.equals(jdbc.query(sql, (ResultSetExtractor<Object>) rs -> rs.next(), email));
    }

    private static Customer map(ResultSet rs) throws SQLException {
        return new Customer(
                (UUID) rs.getObject("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
