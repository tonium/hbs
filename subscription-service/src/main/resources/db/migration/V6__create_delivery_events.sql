CREATE TABLE delivery_events (
    id              BIGSERIAL   PRIMARY KEY,
    message_id      UUID        NOT NULL,
    trace_id        VARCHAR(64) NOT NULL,
    job_id          UUID,
    org_id          VARCHAR(64),
    user_id         VARCHAR(64) NOT NULL,
    program_id      VARCHAR(64) NOT NULL,
    channel_id      VARCHAR(64),
    delivery_stage  VARCHAR(32) NOT NULL,
    delivery_status VARCHAR(16) NOT NULL,
    error_code      VARCHAR(64),
    error_message   TEXT,
    occurred_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_delivery_message
    ON delivery_events (message_id);

CREATE INDEX ix_delivery_user_time
    ON delivery_events (user_id, occurred_at DESC);
