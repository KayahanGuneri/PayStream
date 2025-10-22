-- add headers column for outbox pattern
ALTER TABLE payment.outbox_events
    ADD COLUMN IF NOT EXISTS headers jsonb DEFAULT '{}'::jsonb;
