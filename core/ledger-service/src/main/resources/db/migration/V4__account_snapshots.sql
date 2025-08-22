CREATE TABLE IF NOT EXISTS account_snapshots (
                                                 account_id    UUID        NOT NULL,
                                                 currency      TEXT        NOT NULL,
                                                 balance_minor BIGINT      NOT NULL DEFAULT 0,
                                                 last_offset   BIGINT      NOT NULL DEFAULT 0,
                                                 updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (account_id, currency)
    );
