-- Schema: public
-- Goal: ensure transfers table exists with idempotency_key (unique, NOT NULL)
-- and be resilient if table already exists without the column.

-- We rely on pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
BEGIN
  IF NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = 'public' AND table_name = 'transfers'
  ) THEN
    EXECUTE $CT$
CREATE TABLE public.transfers (
                                  id               UUID PRIMARY KEY,
                                  source_account_id UUID NOT NULL,
                                  dest_account_id   UUID NOT NULL,
                                  currency          VARCHAR(3) NOT NULL,
                                  amount_minor      BIGINT NOT NULL,
                                  status            VARCHAR(24) NOT NULL,
                                  idempotency_key   VARCHAR(80) NOT NULL,
                                  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_transfers_idempotency_key
    ON public.transfers (idempotency_key);
$CT$;
ELSE
    -- Table exists: add column if missing, backfill, enforce NOT NULL + unique
    IF NOT EXISTS (
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema='public' AND table_name='transfers' AND column_name='idempotency_key'
    ) THEN
      EXECUTE 'ALTER TABLE public.transfers ADD COLUMN idempotency_key VARCHAR(80)';
      -- Backfill with generated stable value for existing rows
EXECUTE $BF$
UPDATE public.transfers
SET idempotency_key = COALESCE(idempotency_key, gen_random_uuid()::text)
WHERE idempotency_key IS NULL
    $BF$;
EXECUTE 'ALTER TABLE public.transfers ALTER COLUMN idempotency_key SET NOT NULL';
END IF;

    -- Create unique index if missing
    IF NOT EXISTS (
      SELECT 1 FROM pg_indexes
      WHERE schemaname='public' AND tablename='transfers' AND indexname='ux_transfers_idempotency_key'
    ) THEN
      EXECUTE 'CREATE UNIQUE INDEX ux_transfers_idempotency_key ON public.transfers (idempotency_key)';
END IF;
END IF;
END $$;
