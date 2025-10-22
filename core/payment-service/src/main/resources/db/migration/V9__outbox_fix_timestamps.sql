-- V9__outbox_fix_timestamps.sql
SET search_path TO payment;


ALTER TABLE payment.outbox_events
    ALTER COLUMN created_at SET DEFAULT now();

ALTER TABLE payment.outbox_events
    ADD COLUMN IF NOT EXISTS metadata jsonb NOT NULL DEFAULT '{}'::jsonb;


ALTER TABLE payment.outbox_events
    ALTER COLUMN occurred_at DROP NOT NULL;

ALTER TABLE payment.outbox_events
    ALTER COLUMN occurred_at SET DEFAULT now();

UPDATE payment.outbox_events
SET occurred_at = COALESCE(occurred_at, created_at, now())
WHERE occurred_at IS NULL;

ALTER TABLE payment.outbox_events
    ALTER COLUMN occurred_at SET NOT NULL;


ALTER TABLE payment.outbox_events
    ALTER COLUMN published SET DEFAULT FALSE;


ALTER TABLE payment.outbox_events
    ALTER COLUMN status SET DEFAULT 'NEW';
