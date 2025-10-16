-- V3__payment_captures.sql
CREATE TABLE IF NOT EXISTS payment_captures (
                                                id UUID PRIMARY KEY,
                                                payment_id UUID NOT NULL REFERENCES payments(id),
    amount NUMERIC(18,2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    provider_ref VARCHAR(64),
    idempotency_key VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL
    );
CREATE INDEX IF NOT EXISTS ix_payment_captures_pid ON payment_captures(payment_id, created_at);
