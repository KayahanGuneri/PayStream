
CREATE TABLE IF NOT EXISTS account_balances (
                                                account_id UUID PRIMARY KEY REFERENCES accounts(id) ON DELETE CASCADE,
    current_balance NUMERIC(18,2) NOT NULL DEFAULT 0,
    as_of_ledger_offset BIGINT,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    );
