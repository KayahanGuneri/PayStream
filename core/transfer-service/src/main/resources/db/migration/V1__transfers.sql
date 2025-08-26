CREATE TABLE IF NOT EXISTS transfers (
                                         id                 UUID PRIMARY KEY,
                                         source_account_id  UUID NOT NULL,
                                         dest_account_id    UUID NOT NULL,
                                         currency           TEXT NOT NULL,
                                         amount_minor       BIGINT NOT NULL CHECK (amount_minor > 0),
    idempotency_key    TEXT NOT NULL UNIQUE,
    status             TEXT NOT NULL,
    ledger_tx_id       UUID,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
    );
