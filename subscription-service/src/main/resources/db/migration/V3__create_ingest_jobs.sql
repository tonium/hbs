CREATE TABLE ingest_jobs (
    id         UUID         PRIMARY KEY,
    status     VARCHAR(16)  NOT NULL,
    org_id     VARCHAR(64)  NOT NULL,
    program_id VARCHAR(64)  NOT NULL,
    channel_id VARCHAR(64),
    type       VARCHAR(32)  NOT NULL,
    payload    JSONB        NOT NULL,
    trace_id   VARCHAR(64)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    error_msg  TEXT
);

CREATE INDEX ix_jobs_status_created
    ON ingest_jobs (status, created_at);
