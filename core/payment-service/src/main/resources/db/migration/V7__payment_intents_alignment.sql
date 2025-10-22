-- V7__payment_intents_alignment.sql
-- Purpose:
-- 1) Ensure 'payment_intents' is under 'payment' schema.
-- 2) Keep promoted columns (amount/currency/card_token/idempotency_key) available.
-- 3) Backfill amount from requested_amount if present; keep requested_amount for compatibility.
-- 4) Add defaults for id / created_at; keep useful indexes.

CREATE SCHEMA IF NOT EXISTS payment;

-- Move table to payment schema if created under public
DO $$
BEGIN
  IF to_regclass('payment.payment_intents') IS NULL
     AND to_regclass('public.payment_intents') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.payment_intents SET SCHEMA payment';
END IF;
END$$;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Ensure new columns exist (they were introduced in your V5)
ALTER TABLE payment.payment_intents
    ADD COLUMN IF NOT EXISTS amount           NUMERIC(18,2),
    ADD COLUMN IF NOT EXISTS currency         CHAR(3),
    ADD COLUMN IF NOT EXISTS card_token       VARCHAR(128),
    ADD COLUMN IF NOT EXISTS idempotency_key  VARCHAR(64);

-- Backfill amount <- requested_amount if amount is currently NULL
UPDATE payment.payment_intents
SET amount = requested_amount
WHERE amount IS NULL
  AND requested_amount IS NOT NULL;

-- Defaults
ALTER TABLE payment.payment_intents
    ALTER COLUMN id SET DATA TYPE uuid,
ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE payment.payment_intents
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

-- Indexes
CREATE INDEX IF NOT EXISTS ix_payment_intents_payment_created
    ON payment.payment_intents(payment_id, created_at);

CREATE UNIQUE INDEX IF NOT EXISTS ux_payment_intents_idem
    ON payment.payment_intents(idempotency_key);
