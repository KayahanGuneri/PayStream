
ALTER TABLE outbox_events
    ADD COLUMN IF NOT EXISTS event_type VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_outbox_event_type
    ON outbox_events(event_type);
