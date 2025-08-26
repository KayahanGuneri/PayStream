package com.paystream.transferservice.app;

import com.paystream.transferservice.api.CreateTransferRequest;
import com.paystream.transferservice.domain.NotFoundException;

// Application service: orchestrates idempotency, persistence and Ledger call
package com.paystream.transferservice.app;

import com.paystream.transferservice.api.CreateTransferRequest;
import com.paystream.transferservice.api.TransferResponse;

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

@Service // marks this as a Spring-managed business component

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

        // 2) Simple business guards (extra to @Valid)
        if (req.sourceAccountId().equals(req.destAccountId())) {
            var fail = new Transfer();
            fail.id = UUID.randomUUID();
            fail.status = TransferStatus.FAILED;
            return fail;
        }

        // 3) Create PENDING transfer

    @Transactional // make the whole use-case atomic (DB writes happen together)
    public TransferResponse createTransfer(String idempotencyKey, CreateTransferRequest req) {
        // 1) Idempotency shortcut: if the key already exists, return its stored outcome
        Optional<Transfer> existing = transferDao.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            var e = existing.get();
            return new TransferResponse(e.id, e.status, e.ledgerTxId);
        }

        // 2) Basic input checks (extra guard; main @Valid runs in controller)
        if (req.sourceAccountId().equals(req.destAccountId())) {
            // business rule: source != dest
            var fake = new Transfer(); fake.id = UUID.randomUUID(); // to return a stable shape
            fake.status = TransferStatus.FAILED;
            return new TransferResponse(fake.id, fake.status, null);
        }

        // 3) Create a PENDING row

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
        boolean ok = ledgerClient.appendDoubleEntry(ledgerTxId,
                req.sourceAccountId(), req.destAccountId(),
                req.currency(), req.amountMinor());

        if (!ok) {
            // 6a) Mark FAILED + emit event
            transferDao.markFailed(transferId);
            outboxDao.append("TRANSFER_FAILED", transferId.toString(), null,
                    OutboxPayloads.transferFailed(transferId.toString(), "LEDGER_ERROR"));
            t.status = TransferStatus.FAILED;
            return t;
        }

        // 6b) Mark COMPLETED + emit event
        transferDao.markCompleted(transferId, ledgerTxId);
        outboxDao.append("TRANSFER_COMPLETED", transferId.toString(), req.destAccountId(),
                OutboxPayloads.transferCompleted(transferId.toString(), ledgerTxId.toString()));

        // reflect changes in the returned domain object (kept simple)
        t.status = TransferStatus.COMPLETED;
        t.ledgerTxId = ledgerTxId;
        return t;
    }

    @Transactional(readOnly = true)
    public Transfer getById(UUID id) {
        return transferDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer %s not found".formatted(id)));
    }

        // 4) Move to IN_PROGRESS (optimistic progress marker)
        transferDao.updateStatus(transferId, TransferStatus.IN_PROGRESS);

        // 5) Call Ledger (synchronous for Week 5)
        UUID ledgerTxId = UUID.randomUUID(); // correlation id shared across double-entry rows
        boolean ledgerOk = ledgerClient.appendDoubleEntry(
                ledgerTxId,
                req.sourceAccountId(),
                req.destAccountId(),
                req.currency(),
                req.amountMinor()
        );

        if (!ledgerOk) {
            // 6a) Mark FAILED and return
            transferDao.markFailed(transferId);
            // (optional) emit outbox "transfer.failed"
            outboxDao.append("TRANSFER_FAILED", transferId.toString(), null, """
                {"transferId":"%s"}""".formatted(transferId));
            return new TransferResponse(transferId, TransferStatus.FAILED, null);
        }

        // 6b) Mark COMPLETED, save ledgerTxId
        transferDao.markCompleted(transferId, ledgerTxId);

        // 7) Emit outbox "transfer.completed" (for notifications, reporting, etc.)
        outboxDao.append("TRANSFER_COMPLETED", transferId.toString(), req.destAccountId(), """
            {"transferId":"%s","ledgerTxId":"%s"}""".formatted(transferId, ledgerTxId));

        // 8) Return response
        return new TransferResponse(transferId, TransferStatus.COMPLETED, ledgerTxId);
    }

package com.paystream.transferservice.app;

public class TransferAppService {


}
