-- Add new column in minor units (BIGINT)
ALTER TABLE account_balances
    ADD COLUMN balance_minor BIGINT NOT NULL DEFAULT 0;

-- If you have existing data in NUMERIC(18,2), migrate (TRY -> kuruş)
UPDATE account_balances
SET balance_minor = (current_balance * 100)::BIGINT;

-- Drop old column
ALTER TABLE account_balances DROP COLUMN current_balance;

-- Rename new column to the canonical name
-- (Ledger'deki snapshot ismiyle aynı olsun: balance_minor)
-- (Already named balance_minor, so nothing to rename)
