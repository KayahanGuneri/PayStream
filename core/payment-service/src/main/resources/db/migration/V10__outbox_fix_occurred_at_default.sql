UPDATE payment.outbox_events
SET occurred_at = COALESCE(occurred_at, created_at, now())
WHERE occurred_at IS NULL;

ALTER TABLE payment.outbox_events
    ALTER COLUMN occurred_at SET DEFAULT now();

ALTER TABLE payment.outbox_events
    ALTER COLUMN occurred_at SET NOT NULL;
