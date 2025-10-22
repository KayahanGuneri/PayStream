-- V8__payment_captures_defaults.sql
-- Purpose:
-- 1) Ensure 'payment_captures' lives under 'payment' schema.
-- 2) Add defaults for id / created_at.
-- 3) Keep useful indexes.

CREATE SCHEMA IF NOT EXISTS payment;

-- Move table to payment schema if created under public
DO $$
BEGIN
  IF to_regclass('payment.payment_captures') IS NULL
     AND to_regclass('public.payment_captures') IS NOT NULL THEN
    EXECUTE 'ALTER TABLE public.payment_captures SET SCHEMA payment';
END IF;
END$$;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE payment.payment_captures
    ALTER COLUMN id SET DATA TYPE uuid,
ALTER COLUMN id SET DEFAULT gen_random_uuid();

ALTER TABLE payment.payment_captures
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS ix_payment_captures_payment_created
    ON payment.payment_captures(payment_id, created_at);
