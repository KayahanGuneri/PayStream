-- V2__payment_intents.sql
CREATE TABLE IF NOT EXISTS payment_intents (
                                               id UUID PRIMARY KEY,
                                               payment_id UUID NOT NULL REFERENCES payments(id),
    requested_amount NUMERIC(18,2) NOT NULL,
    requires_3ds BOOLEAN NOT NULL,
    rba_reason VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL
    );
CREATE INDEX IF NOT EXISTS ix_payment_intents_pid ON payment_intents(payment_id, created_at);
