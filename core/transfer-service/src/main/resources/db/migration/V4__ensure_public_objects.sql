CREATE TABLE IF NOT EXISTS public.transfers (
                                                id                UUID PRIMARY KEY,
                                                source_account_id UUID NOT NULL,
                                                dest_account_id   UUID NOT NULL,
                                                currency          TEXT NOT NULL,
                                                amount_minor      BIGINT NOT NULL CHECK (amount_minor > 0),
    idempotency_key   TEXT NOT NULL UNIQUE,
    status            TEXT NOT NULL,
    ledger_tx_id      UUID,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_transfers_src     ON public.transfers(source_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_dest    ON public.transfers(dest_account_id);
CREATE INDEX IF NOT EXISTS idx_transfers_status  ON public.transfers(status);

CREATE TABLE IF NOT EXISTS public.transfer_steps (
                                                     id          UUID PRIMARY KEY,
                                                     transfer_id UUID NOT NULL,
                                                     from_state  TEXT NOT NULL,
                                                     to_state    TEXT NOT NULL,
                                                     reason      TEXT,
                                                     created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    );
CREATE INDEX IF NOT EXISTS idx_transfer_steps_transfer_id
    ON public.transfer_steps(transfer_id);

