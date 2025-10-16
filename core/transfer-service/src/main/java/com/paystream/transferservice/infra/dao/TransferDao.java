package com.paystream.transferservice.infra.dao;

import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import com.paystream.transferservice.infra.mapper.TransferRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TransferDao {

    private final JdbcTemplate jdbc;
    private static final TransferRowMapper MAPPER = new TransferRowMapper();

    public TransferDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Transfer> findById(UUID id) {
        String sql = """
            SELECT id, source_account_id, dest_account_id, currency, amount_minor,
                   status, idempotency_key, created_at, updated_at, ledger_tx_id
            FROM public.transfers
            WHERE id = ?
            """;
        List<Transfer> list = jdbc.query(sql, MAPPER, id);
        return list.stream().findFirst();
    }

    public Optional<Transfer> findByIdempotencyKey(String key) {
        String sql = """
            SELECT id, source_account_id, dest_account_id, currency, amount_minor,
                   status, idempotency_key, created_at, updated_at, ledger_tx_id
            FROM public.transfers
            WHERE idempotency_key = ?
            """;
        List<Transfer> list = jdbc.query(sql, MAPPER, key);
        return list.stream().findFirst();
    }

    public void insertPending(Transfer t) {
        jdbc.update("""
            INSERT INTO public.transfers
              (id, source_account_id, dest_account_id, currency, amount_minor, idempotency_key, status)
            VALUES (?,?,?,?,?,?,?)
            """,
                t.id(),                 // record accessor!
                t.sourceAccountId(),
                t.destAccountId(),
                t.currency(),
                t.amountMinor(),
                t.idempotencyKey(),
                t.status().name()
        );
    }

    public void updateStatus(UUID id, TransferStatus status) {
        jdbc.update("UPDATE public.transfers SET status = ?, updated_at = now() WHERE id = ?",
                status.name(), id);
    }

    public void markFailed(UUID id) {
        updateStatus(id, TransferStatus.FAILED);
    }

    public void markCompleted(UUID id, UUID ledgerTxId) {
        jdbc.update("""
            UPDATE public.transfers
               SET status = ?, ledger_tx_id = ?, updated_at = now()
             WHERE id = ?
            """,
                TransferStatus.COMPLETED.name(), ledgerTxId, id
        );
    }
}
