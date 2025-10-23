DO $$
BEGIN
  IF to_regclass('account.outbox_events') IS NOT NULL THEN
ALTER TABLE account.outbox_events
ALTER COLUMN aggregate_id TYPE uuid USING aggregate_id::uuid;

    IF EXISTS (
      SELECT 1 FROM information_schema.columns
      WHERE table_schema='account' AND table_name='outbox_events' AND column_name='key_account_id'
    ) THEN
ALTER TABLE account.outbox_events
ALTER COLUMN key_account_id TYPE uuid USING key_account_id::uuid;
END IF;
END IF;
END$$;
