CREATE TABLE outbox_events (
    id           BIGSERIAL    PRIMARY KEY,
    event_type   VARCHAR(64)  NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    payload      JSONB        NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ
);

CREATE INDEX ix_outbox_unprocessed
    ON outbox_events (processed_at)
    WHERE processed_at IS NULL;
