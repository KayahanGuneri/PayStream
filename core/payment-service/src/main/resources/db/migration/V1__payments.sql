-- V1__payments.sql
CREATE TABLE IF NOT EXISTS payments (
                                        id UUID PRIMARY KEY,
                                        merchant_id UUID NOT NULL,
                                        amount NUMERIC(18,2) NOT NULL,
    currency CHAR(3) NOT NULL,
    status VARCHAR(24) NOT NULL,
    card_token VARCHAR(128) NOT NULL,
    risk_score INT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    provider_ref VARCHAR(64)
    );
CREATE UNIQUE INDEX IF NOT EXISTS ux_payments_merchant_idem
    ON payments(merchant_id, idempotency_key);
CREATE INDEX IF NOT EXISTS ix_payments_status ON payments(status);
