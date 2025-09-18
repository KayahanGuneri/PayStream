CREATE TABLE IF NOT EXISTS public.outbox_events (
                                                    id             BIGSERIAL PRIMARY KEY,
                                                    aggregate_type TEXT NOT NULL,
                                                    aggregate_id   TEXT NOT NULL,
                                                    key_account_id UUID,
                                                    payload        JSONB NOT NULL,
                                                    status         TEXT NOT NULL DEFAULT 'NEW',
                                                    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at   TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_outbox_new
    ON public.outbox_events(status)
    WHERE status = 'NEW';
