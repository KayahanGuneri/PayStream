-- V6__payments_defaults_and_schema.sql
-- Purpose:
-- 1) Ensure 'payments' is under 'payment' schema.
-- 2) Add safe DB defaults for id / created_at / updated_at.
-- 3) Keep indexes consistent.

CREATE SCHEMA IF NOT EXISTS payment;

-- Move public.payments -> payment.payments if needed.
DO $$
BEGIN
  IF to_regclass('payment.payments') IS NULL
     AND to_regclass('public.payments') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.payments SET SCHEMA payment';
END IF;
END$$;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE payment.payments
    ALTER COLUMN id SET DATA TYPE uuid,
ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE payment.payments
    ALTER COLUMN created_at SET DEFAULT now(),
ALTER COLUMN updated_at SET DEFAULT now();

CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_merchant_idem
    ON payment.payments(merchant_id, idempotency_key);

CREATE INDEX IF NOT EXISTS ix_payments_status
    ON payment.payments(status);
