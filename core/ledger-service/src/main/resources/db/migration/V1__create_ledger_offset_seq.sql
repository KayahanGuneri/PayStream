-- Global, monotonically increasing sequence used to assign a unique ledger_offset
-- to each appended ledger entry. This offset is included in events and used by
-- consumers to apply idempotent snapshot updates (apply only if offset is newer).
CREATE SEQUENCE IF NOT EXISTS ledger_offset_seq
    AS BIGINT
    INCREMENT BY 1
    START WITH 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 100;
