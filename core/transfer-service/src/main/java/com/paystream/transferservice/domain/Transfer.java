// Minimal domain entity for persistence + business transitions
package com.paystream.transferservice.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Transfer {
    // NOTE: Fields are kept public for minimal diff; we also expose accessor methods
    // so that callers can use record-like syntax (e.g., transfer.sourceAccountId()).
    public UUID id;
    public UUID sourceAccountId;
    public UUID destAccountId;
    public String currency;
    public long amountMinor;
    public String idempotencyKey;
    public TransferStatus status;
    public UUID ledgerTxId;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;

    // --- Factory ---

    /** Creates a new PENDING transfer with normalized currency (ISO-4217 uppercase). */
    public static Transfer pending(UUID id, UUID source, UUID dest, String currency, long amount, String idemKey) {
        var t = new Transfer();
        t.id = id;
        t.sourceAccountId = source;
        t.destAccountId = dest;
        t.currency = currency == null ? null : currency.toUpperCase(); // normalize to ISO uppercase
        t.amountMinor = amount;
        t.idempotencyKey = idemKey;
        t.status = TransferStatus.PENDING;
        return t;
    }

    // --- Accessors (record-like) ---

    /** Domain accessor for id (record-like). */
    public UUID id() { return id; }

    /** Domain accessor for source account id (record-like). */
    public UUID sourceAccountId() { return sourceAccountId; }

    /** Domain accessor for destination account id (record-like). */
    public UUID destAccountId() { return destAccountId; }

    /** Domain accessor for ISO currency (record-like). */
    public String currency() { return currency; }

    /** Domain accessor for amount (minor units) (record-like). */
    public long amountMinor() { return amountMinor; }

    /** Domain accessor for idempotency key (record-like). */
    public String idempotencyKey() { return idempotencyKey; }

    /** Domain accessor for status (record-like). */
    public TransferStatus status() { return status; }

    /** Domain accessor for ledger transaction id (record-like). */
    public UUID ledgerTxId() { return ledgerTxId; }

    // --- Domain mutators (keep transitions explicit) ---

    /** Sets status in a controlled way (prefer using FSM guard before calling this). */
    public void setStatus(TransferStatus newStatus) { this.status = newStatus; }

    /** Sets ledger transaction id once the ledger append succeeds. */
    public void setLedgerTxId(UUID ledgerTxId) { this.ledgerTxId = ledgerTxId; }

    // (Optional) You could add helper methods like complete()/fail() later if you want stricter invariants.
}
