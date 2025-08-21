package com.paystream.ledgerservice.infra.mapper;

import com.paystream.ledgerservice.domain.LedgerEntry;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Component // Makes this RowMapper injectable (useful for tests and reuse)
public class LedgerEntryRowMapper implements RowMapper<LedgerEntry> {

    @Override // Maps a JDBC ResultSet row into a LedgerEntry domain object
    public LedgerEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        return LedgerEntry.builder()
                .entryId((UUID) rs.getObject("entry_id"))        // primary key
                .txId((UUID) rs.getObject("tx_id"))              // transaction id
                .txSeq(rs.getInt("tx_seq"))                      // sequence within the transaction
                .accountId((UUID) rs.getObject("account_id"))    // account id
                .currency(rs.getString("currency"))              // currency code
                .amountMinor(rs.getLong("amount_minor"))         // signed minor units
                .ledgerOffset(rs.getLong("ledger_offset"))       // global ordering offset
                .createdAt(rs.getObject("created_at", OffsetDateTime.class)) // server timestamp
                .build();
    }
}
