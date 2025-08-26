// Low-level SQL operations for the transfers table (no business logic here).
package com.paystream.transferservice.infra.dao;

import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import com.paystream.transferservice.infra.mapper.TransferRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository // persistence component with exception translation
public class TransferDao {

    private final JdbcTemplate jdbc;
    private static final TransferRowMapper MAPPER = new TransferRowMapper();

    public TransferDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Optional<Transfer> findById(UUID id) {
        var list = jdbc.query("SELECT * FROM transfers WHERE id = ?", MAPPER, id);
        return list.stream().findFirst();
    }

    public Optional<Transfer> findByIdempotencyKey(String key) {
        var list = jdbc.query("SELECT * FROM transfers WHERE idempotency_key = ?", MAPPER, key);
        return list.stream().findFirst();
    }

    public void insertPending(Transfer t) {
        jdbc.update("""
            INSERT INTO transfers(id, source_account_id, dest_account_id, currency, amount_minor, idempotency_key, status)
            VALUES (?,?,?,?,?,?,?)
        """, t.id, t.sourceAccountId, t.destAccountId, t.currency, t.amountMinor, t.idempotencyKey, t.status.name());
    }

    public void updateStatus(UUID id, TransferStatus status) {
        jdbc.update("UPDATE transfers SET status=?, updated_at=now() WHERE id=?", status.name(), id);
    }

    public void markFailed(UUID id) {
        updateStatus(id, TransferStatus.FAILED);
    }

    public void markCompleted(UUID id, UUID ledgerTxId) {
        jdbc.update("UPDATE transfers SET status=?, ledger_tx_id=?, updated_at=now() WHERE id=?",
                TransferStatus.COMPLETED.name(), ledgerTxId, id);
    }
}
