-- V5__align_outbox_with_code_and_defaults.sql
-- Purpose:
-- 1) Force outbox_events to live under 'payment' schema (avoid public/payment split).
-- 2) Align column names to code (headers -> metadata).
-- 3) Add DB-side defaults: id (uuid), created_at (now), published (boolean).
-- 4) Non-destructive: no drops, only fixes / adds.

CREATE SCHEMA IF NOT EXISTS payment;

-- Move public.outbox_events to payment if needed (keeps data).
DO $$
BEGIN
  IF to_regclass('payment.outbox_events') IS NULL
     AND to_regclass('public.outbox_events') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.outbox_events SET SCHEMA payment';
END IF;
END$$;

-- Enable DB-side UUID generation.
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- headers -> metadata (match code)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema='payment' AND table_name='outbox_events' AND column_name='headers'
  ) THEN
    EXECUTE 'ALTER TABLE payment.outbox_events RENAME COLUMN headers TO metadata';
END IF;
END$$;

-- created_at default
ALTER TABLE payment.outbox_events
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- published boolean (code expects a boolean flag)
ALTER TABLE payment.outbox_events
    ADD COLUMN IF NOT EXISTS published BOOLEAN NOT NULL DEFAULT FALSE;

-- optional error column (for dispatcher failures)
ALTER TABLE payment.outbox_events
    ADD COLUMN IF NOT EXISTS error TEXT;

-- PK default (critical: prevent NULL id insert)
ALTER TABLE payment.outbox_events
    ALTER COLUMN id SET DATA TYPE uuid,
ALTER COLUMN id SET DEFAULT gen_random_uuid();

-- indexes (idempotent)
CREATE INDEX IF NOT EXISTS ix_outbox_status
    ON payment.outbox_events(status);

CREATE INDEX IF NOT EXISTS ix_outbox_agg_created
    ON payment.outbox_events(aggregate_id, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_outbox_status_created
    ON payment.outbox_events(status, created_at);
