CREATE TABLE IF NOT EXISTS outbox_events (
                                             id UUID PRIMARY KEY,
                                             aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    headers JSONB,
    occurred_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP NULL
    );

CREATE INDEX IF NOT EXISTS idx_outbox_agg ON outbox_events(aggregate_id);
CREATE INDEX IF NOT EXISTS idx_outbox_event_type ON outbox_events(event_type);
CREATE INDEX IF NOT EXISTS idx_outbox_occurred ON outbox_events(occurred_at);
