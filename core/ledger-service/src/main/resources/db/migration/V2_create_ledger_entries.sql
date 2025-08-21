-- Append-only table that stores individual ledger lines (double-entry).
-- Each financial transaction (tx_id) consists of at least two entries whose
-- signed amounts sum to zero (+debit / -credit). Updates/Deletes are disallowed
-- by convention; corrections are done via reversal entries.
CREATE TABLE IF NOT EXISTS ledger_entries (
                                              entry_id         UUID         PRIMARY KEY,
                                              tx_id            UUID         NOT NULL,           -- correlates lines of the same transaction
                                              tx_seq           INT          NOT NULL,           -- sequence number within the transaction
                                              account_id       UUID         NOT NULL,           -- affected account
                                              currency         TEXT         NOT NULL,           -- ISO-like code (e.g., TRY, USD)
                                              amount_minor     BIGINT       NOT NULL,           -- signed amount in minor units (+debit / -credit)
                                              created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    ledger_offset    BIGINT       NOT NULL,           -- global ordering from ledger_offset_seq

    CONSTRAINT uq_tx_seq UNIQUE (tx_id, tx_seq),
    CONSTRAINT ck_amount_nonzero CHECK (amount_minor <> 0)
    );

-- Global ordering must be unique to guarantee deterministic application on consumers.
CREATE UNIQUE INDEX IF NOT EXISTS uq_ledger_offset ON ledger_entries(ledger_offset);

-- Common access patterns:
--  - per account & currency (snapshot and reconciliation)
--  - per transaction (audit and debugging)
CREATE INDEX IF NOT EXISTS ix_ledger_account_currency ON ledger_entries(account_id, currency);
CREATE INDEX IF NOT EXISTS ix_ledger_tx ON ledger_entries(tx_id);
