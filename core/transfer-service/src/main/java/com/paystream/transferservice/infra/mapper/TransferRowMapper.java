// Maps a JDBC ResultSet row to the domain Transfer object (SRP-friendly).
package com.paystream.transferservice.infra.mapper;

import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TransferRowMapper implements RowMapper<Transfer> {
    @Override
    public Transfer mapRow(ResultSet rs, int rowNum) throws SQLException {
        var t = new Transfer();
        t.id = UUID.fromString(rs.getString("id"));
        t.sourceAccountId = UUID.fromString(rs.getString("source_account_id"));
        t.destAccountId = UUID.fromString(rs.getString("dest_account_id"));
        t.currency = rs.getString("currency");
        t.amountMinor = rs.getLong("amount_minor");
        t.idempotencyKey = rs.getString("idempotency_key");
        t.status = TransferStatus.valueOf(rs.getString("status"));
        var ledgerTx = rs.getString("ledger_tx_id");
        t.ledgerTxId = (ledgerTx == null) ? null : UUID.fromString(ledgerTx);
        t.createdAt = rs.getObject("created_at", java.time.OffsetDateTime.class);
        t.updatedAt = rs.getObject("updated_at", java.time.OffsetDateTime.class);
        return t;
    }
}
