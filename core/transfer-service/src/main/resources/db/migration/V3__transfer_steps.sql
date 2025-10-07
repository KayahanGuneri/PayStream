-- Stores every legal state change for observability and post-mortems
CREATE TABLE IF NOT EXISTS transfer_steps (
                                              id            UUID PRIMARY KEY,
                                              transfer_id   UUID NOT NULL,
                                              from_state    TEXT NOT NULL,
                                              to_state      TEXT NOT NULL,
                                              reason        TEXT,
                                              created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_transfer_steps_transfer_id
    ON transfer_steps(transfer_id);
