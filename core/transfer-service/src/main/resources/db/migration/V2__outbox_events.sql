CREATE TABLE IF NOT EXISTS outbox_events (
                                             id              BIGSERIAL PRIMARY KEY,
                                             aggregate_type  TEXT        NOT NULL,
                                             aggregate_id    UUID        NOT NULL,
                                             key_account_id  UUID        NULL,
                                             payload_json    JSONB       NOT NULL,
                                             status          TEXT        NOT NULL,       -- NEW / PUBLISHED / FAILED ...
                                             created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate_id ON outbox_events(aggregate_id);
