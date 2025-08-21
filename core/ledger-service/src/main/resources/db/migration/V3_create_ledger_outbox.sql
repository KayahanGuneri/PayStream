-- Outbox table to publish one event PER ENTRY.
-- We publish per-entry so we can use account_id as the Kafka partition key,
-- guaranteeing in-partition ordering for a given account.
CREATE TABLE IF NOT EXISTS ledger_outbox (
                                             id              UUID         PRIMARY KEY,
                                             event_type      TEXT         NOT NULL,       -- e.g., 'ledger.entry.appended'
                                             key_account_id  UUID         NOT NULL,       -- Kafka key (partition key)
                                             payload         JSONB        NOT NULL,       -- serialized event including ledger_offset
                                             created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ  NULL            -- set when successfully published
    );

-- Fast scan for unpublished events
CREATE INDEX IF NOT EXISTS ix_outbox_unpublished
    ON ledger_outbox(published_at) WHERE published_at IS NULL;
