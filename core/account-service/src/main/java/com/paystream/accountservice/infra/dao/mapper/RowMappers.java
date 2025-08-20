package com.paystream.accountservice.infra.dao.mapper;

import com.paystream.accountservice.domain.account.Account;
import com.paystream.accountservice.domain.account.AccountBalance;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class RowMappers {

    public static final RowMapper<Account> ACCOUNT = (rs, rowNum) -> new Account(
            getUuid(rs, "id"),
            getUuid(rs, "customer_id"),
            rs.getString("currency"),
            rs.getString("status"),
            rs.getLong("version"), //This column not be empty
            rs.getTimestamp("created_at").toInstant(),
            rs.getTimestamp("updated_at").toInstant()
    );

    public static final RowMapper<AccountBalance> ACCOUNT_BALANCE = (rs, rowNum) -> new AccountBalance(
            getUuid(rs, "account_id"),
            rs.getBigDecimal("current_balance"),
            (Long) rs.getObject("as_of_ledger_offset"), //This column maybe be empty
            rs.getTimestamp("updated_at").toInstant()
    );

    private static UUID getUuid(ResultSet rs, String col) throws SQLException {
        Object o = rs.getObject(col);
        if (o instanceof UUID u) return u;
        return UUID.fromString(String.valueOf(o));
    }
}
