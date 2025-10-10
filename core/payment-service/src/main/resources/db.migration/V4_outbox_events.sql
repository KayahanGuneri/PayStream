-- V4__outbox_events.sql
CREATE TABLE IF NOT EXISTS outbox_events (
                                             id UUID PRIMARY KEY,
                                             aggregate_type VARCHAR(32) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    headers JSONB,
    occurred_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    status VARCHAR(12) NOT NULL DEFAULT 'NEW'
    );
CREATE INDEX IF NOT EXISTS ix_outbox_status_new ON outbox_events(status);
CREATE INDEX IF NOT EXISTS ix_outbox_agg ON outbox_events(aggregate_id, occurred_at DESC);
