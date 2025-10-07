package com.paystream.transferservice.app;

import com.paystream.transferservice.api.CreateTransferRequest;
import com.paystream.transferservice.domain.NotFoundException;
import com.paystream.transferservice.domain.Transfer;
import com.paystream.transferservice.domain.TransferStatus;
import com.paystream.transferservice.infra.client.LedgerClient;
import com.paystream.transferservice.infra.dao.OutboxDao;
import com.paystream.transferservice.infra.dao.TransferDao;
import com.paystream.transferservice.infra.mapper.OutboxPayloads;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service // business component managed by Spring
public class TransferAppService {

    private final TransferDao transferDao;
    private final LedgerClient ledgerClient;
    private final OutboxDao outboxDao;

    public TransferAppService(TransferDao transferDao, LedgerClient ledgerClient, OutboxDao outboxDao) {
        this.transferDao = transferDao;
        this.ledgerClient = ledgerClient;
        this.outboxDao = outboxDao;
    }

    @Transactional // atomic boundary: DB writes succeed/fail together
    public Transfer createTransfer(String idempotencyKey, CreateTransferRequest req) {
        // 1) Idempotency: if a record already exists for this key, return it
        Optional<Transfer> existing = transferDao.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        // 2) Basic guard: source and destination cannot be the same
        if (req.sourceAccountId().equals(req.destAccountId())) {
            var fail = new Transfer();
            fail.id = UUID.randomUUID();
            fail.status = TransferStatus.FAILED;
            return fail;
        }

        // 3) Create PENDING transfer
        UUID transferId = UUID.randomUUID();
        Transfer t = Transfer.pending(
                transferId,
                req.sourceAccountId(),
                req.destAccountId(),
                req.currency(),
                req.amountMinor(),
                idempotencyKey
        );
        transferDao.insertPending(t);

        // 4) Move to IN_PROGRESS
        transferDao.updateStatus(transferId, TransferStatus.IN_PROGRESS);

        // 5) Call Ledger (synchronous in Week 5)
        UUID ledgerTxId = UUID.randomUUID();
        boolean ok = ledgerClient.appendDoubleEntry(
                ledgerTxId,
                req.sourceAccountId(),
                req.destAccountId(),
                req.currency(),
                req.amountMinor()
        );

        if (!ok) {
            // 6a) Mark FAILED + emit outbox event
            transferDao.markFailed(transferId);

            // NOTE: OutboxDao expects UUIDs now (no toString for ids)
            outboxDao.append(
                    "TRANSFER_FAILED",
                    transferId,                   // aggregateId (UUID)
                    null,                         // keyAccountId can be null if schema allows
                    OutboxPayloads.transferFailed(
                            transferId.toString(), // JSON keeps string form
                            "LEDGER_ERROR"
                    )
            );

            t.status = TransferStatus.FAILED;
            return t;
        }

        // 6b) Mark COMPLETED + emit outbox event
        transferDao.markCompleted(transferId, ledgerTxId);

        outboxDao.append(
                "TRANSFER_COMPLETED",
                transferId,                    // aggregateId (UUID)
                req.destAccountId(),           // keyAccountId (UUID)
                OutboxPayloads.transferCompleted(
                        transferId.toString(),  // JSON payload as string
                        ledgerTxId.toString()
                )
        );

        // reflect changes in the returned domain object
        t.status = TransferStatus.COMPLETED;
        t.ledgerTxId = ledgerTxId;
        return t;
    }

    @Transactional(readOnly = true)
    public Transfer getById(UUID id) {
        return transferDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer %s not found".formatted(id)));
    }
}
