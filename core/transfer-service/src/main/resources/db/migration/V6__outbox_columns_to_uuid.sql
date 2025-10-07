ALTER TABLE account.outbox_events
ALTER COLUMN aggregate_id   TYPE uuid USING aggregate_id::uuid,
  ALTER COLUMN key_account_id TYPE uuid USING key_account_id::uuid;
