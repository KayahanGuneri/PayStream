package com.paystream.transferservice.app;

import com.paystream.transferservice.api.CreateTransferRequest;
import com.paystream.transferservice.domain.*;
import com.paystream.transferservice.infra.client.LedgerClient;
import com.paystream.transferservice.infra.dao.OutboxDao;
import com.paystream.transferservice.infra.dao.TransferDao;
import com.paystream.transferservice.infra.mapper.OutboxPayloads;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.paystream.transferservice.domain.TransferStatus.*;

@Service
@RequiredArgsConstructor
public class TransferAppService {

    private final TransferDao transferDao;          // persistence for transfers
    private final LedgerClient ledgerClient;        // synchronous double-entry call
    private final OutboxDao outboxDao;              // outbox event producer
    private final TransferStepRepository stepRepo;  // FSM audit writer

    private final TransferDao transferDao;
    private final LedgerClient ledgerClient;
    private final OutboxDao outboxDao;
    private final TransferStepRepository stepRepo;

    @Transactional
    public Transfer createTransfer(String idemKey, CreateTransferRequest req) {
        // (1) Basic input rules (simple checks before DB)
        validateBusiness(req);

        // (2) Quick check: is this key already used?
        Optional<Transfer> found = transferDao.findByIdempotencyKey(idemKey);
        if (found.isPresent()) {
            if (!sameBody(found.get(), req)) {
                throw new IdempotencyConflictException("Same key, different request body");
            }
            return found.get();
        }

        // (3) Build a new transfer with status PENDING

    private final TransferDao transferDao;                 // persistence for transfers
    private final LedgerClient ledgerClient;               // synchronous double-entry call
    private final OutboxDao outboxDao;                     // outbox event producer
    private final TransferStepRepository stepRepo;         // FSM audit writer (DIP)


    @Transactional
    public Transfer createTransfer(String idemKey, CreateTransferRequest req) {
        // 0) business validation
        validateBusiness(req);

        // 1) idempotency: same key -> same response (or 409 on body mismatch)
        Optional<Transfer> found = transferDao.findByIdempotencyKey(idemKey);
        if (found.isPresent()) {
            if (!sameBody(found.get(), req)) {
                throw new IdempotencyConflictException("Same key, different request body");
            }
            return found.get();
        }

        // 2) create aggregate in PENDING
        UUID transferId = UUID.randomUUID();
        Transfer t = Transfer.pending(
                transferId,
                req.sourceAccountId(),
                req.destAccountId(),
                req.currency(),
                req.amountMinor(),
                idemKey
        );


        try {
            transferDao.insertPending(t);
        } catch (DataIntegrityViolationException e) {
            Transfer existing = transferDao.findByIdempotencyKey(idemKey)
                    .orElseThrow(() -> e);
            if (!sameBody(existing, req)) {
                throw new IdempotencyConflictException("Same key, different request body");
            }
            return existing;
        }

        // (4) State change: PENDING → IN_PROGRESS
        fsmTransition(t, PENDING, IN_PROGRESS, "accepted");
        transferDao.updateStatus(transferId, IN_PROGRESS);

        // (5) Call ledger

        try {
            transferDao.insertPending(t);
        } catch (DataIntegrityViolationException e) {
            // race: inserted by another thread/request
            Transfer existing = transferDao.findByIdempotencyKey(idemKey).orElseThrow(() -> e);
            if (!sameBody(existing, req)) {
                throw new IdempotencyConflictException("Same key, different request body");
            }
            return existing;
        }

        // 3) FSM: PENDING -> IN_PROGRESS
        fsmTransition(t, PENDING, IN_PROGRESS, "accepted");
        transferDao.updateStatus(transferId, IN_PROGRESS);

        // 4) call ledger
        UUID ledgerTxId = UUID.randomUUID();
        boolean ok;
        try {
            ok = ledgerClient.appendDoubleEntry(
                    ledgerTxId,
                    req.sourceAccountId(),
                    req.destAccountId(),
                    req.currency(),
                    req.amountMinor()
            );
        } catch (Exception ex) {

        } catch (InsufficientFundsException ife) {
            ok = false; // business failure
        } catch (Exception any) {
            ok = false; // infra error -> fail in MVP
        }

        if (!ok) {
            // 5a) failure path

            // (6a) Failure path

            // 5a) FSM: IN_PROGRESS -> FAILED (guard + audit) + persist + event
            fsmTransition(t, IN_PROGRESS, FAILED, "ledger_error");
            transferDao.markFailed(transferId);
            outboxDao.append(
                    "TRANSFER_FAILED",
                    transferId,
                    null, // key_account_id yok (bilinçli)

                    null,  // key_account_id bilinçli null olabilir
                    OutboxPayloads.transferFailed(transferId.toString(), "LEDGER_ERROR")
            );
            t.status = FAILED;
            return t;
        }

        // (6b) Success path

                    transferId.toString(),
                    null,
                    OutboxPayloads.transferFailed(transferId.toString(), "LEDGER_ERROR")
            );
            t.status = FAILED;
            return t;
        }

        // 5b) success path
        fsmTransition(t, IN_PROGRESS, COMPLETED, "ok");
        transferDao.markCompleted(transferId, ledgerTxId);
        outboxDao.append(
                "TRANSFER_COMPLETED",
                transferId,
                req.destAccountId(), // event key için hedef hesap

                req.destAccountId(), // artık UUID olarak geçiyoruz
                transferId.toString(),
                req.destAccountId(),
                OutboxPayloads.transferCompleted(transferId.toString(), ledgerTxId.toString())
        );
        t.status = COMPLETED;
        t.ledgerTxId = ledgerTxId;
        return t;
    }

    @Transactional(readOnly = true)
    public Transfer getById(UUID id) {
        return transferDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Transfer %s not found".formatted(id)));
    }

    // -- Helpers --

    private void validateBusiness(CreateTransferRequest req) {
        if (Objects.equals(req.sourceAccountId(), req.destAccountId())) {
            throw new DomainValidationException("sourceAccountId must differ from destAccountId");

    // -------- helpers --------

    private void validateBusiness(CreateTransferRequest req) {
        if (Objects.equals(req.sourceAccountId(), req.destAccountId())) {
            throw new DomainValidationException("sourceAccountId must be different from destAccountId");
        }
        if (req.amountMinor() <= 0) {
            throw new DomainValidationException("amountMinor must be > 0");
        }
        if (req.currency() == null || req.currency().length() != 3) {
            throw new DomainValidationException("currency must be 3 letters (e.g. USD)");

        }
    }


        if (req.currency() == null || req.currency().length() != 3
                || !req.currency().equals(req.currency().toUpperCase())) {
            throw new DomainValidationException("currency must be 3-letter ISO uppercase (e.g., TRY, USD)");
        }
    }

    /** Compares the persisted transfer with the incoming request under the same idem key. */
    private boolean sameBody(Transfer existing, CreateTransferRequest req) {
        return existing.sourceAccountId().equals(req.sourceAccountId())
                && existing.destAccountId().equals(req.destAccountId())
                && existing.currency().equalsIgnoreCase(req.currency())
                && existing.amountMinor() == req.amountMinor();
    }

    /** Guards FSM and writes an audit step. */

    private void fsmTransition(Transfer t, TransferStatus from, TransferStatus to, String reason) {
        TransferStateMachine.enforce(from, to);

    /** Guards FSM and writes an audit step. DB status update is performed by DAO at call sites. */
    private void fsmTransition(Transfer t, TransferStatus from, TransferStatus to, String reason) {
        TransferStateMachine.enforce(from, to);
        stepRepo.save(UUID.randomUUID(), t.id(), from, to, reason);
    }
}
