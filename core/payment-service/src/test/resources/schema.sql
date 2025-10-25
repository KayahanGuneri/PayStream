CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE IF NOT EXISTS payment.payment_intents (
                                                       id               UUID        NOT NULL,
                                                       payment_id       UUID        NOT NULL,
                                                       requested_amount NUMERIC(19,2) NOT NULL,
    requires_3ds     BOOLEAN     NOT NULL,
    rba_reason       VARCHAR(100),
    created_at       TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
    );
