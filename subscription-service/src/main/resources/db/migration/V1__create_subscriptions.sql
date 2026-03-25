CREATE TABLE subscriptions (
    id           BIGSERIAL    PRIMARY KEY,
    org_id       VARCHAR(64)  NOT NULL,
    user_id      VARCHAR(64)  NOT NULL,
    program_id   VARCHAR(64)  NOT NULL,
    channel_id   VARCHAR(64),
    status       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_subscriptions
    ON subscriptions (org_id, user_id, program_id, COALESCE(channel_id, 'all'));

CREATE INDEX ix_sub_by_prog
    ON subscriptions (org_id, program_id, COALESCE(channel_id, 'all'), status);

CREATE INDEX ix_sub_by_user
    ON subscriptions (org_id, user_id, status);
