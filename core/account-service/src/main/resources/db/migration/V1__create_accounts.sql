CREATE TABLE IF NOT EXISTS accounts (
                                        id UUID PRIMARY KEY,
                                        customer_id UUID NOT NULL,
                                        currency VARCHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_accounts_customer ON accounts(customer_id);
