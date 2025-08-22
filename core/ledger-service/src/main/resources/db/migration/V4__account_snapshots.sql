-- Snapshot tablosu: hesap + para birimi başına bakiye ve en son uygulanan offset
CREATE TABLE IF NOT EXISTS account_snapshots (
                                                 account_id     UUID        NOT NULL,
                                                 currency       TEXT        NOT NULL,
                                                 balance_minor  BIGINT      NOT NULL DEFAULT 0,
                                                 last_offset    BIGINT      NOT NULL DEFAULT 0,
                                                 PRIMARY KEY (account_id, currency)
    );


