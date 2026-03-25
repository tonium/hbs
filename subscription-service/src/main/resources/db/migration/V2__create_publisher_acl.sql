CREATE TABLE publisher_acl (
    id           BIGSERIAL    PRIMARY KEY,
    org_id       VARCHAR(64)  NOT NULL,
    subject_type VARCHAR(16)  NOT NULL,
    subject_id   VARCHAR(128) NOT NULL,
    program_id   VARCHAR(64)  NOT NULL,
    channel_id   VARCHAR(64),
    status       VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_pub_acl
    ON publisher_acl (org_id, subject_type, subject_id, program_id, COALESCE(channel_id, 'all'));

CREATE INDEX ix_pub_acl_lookup
    ON publisher_acl (org_id, subject_type, subject_id, program_id, COALESCE(channel_id, 'all'), status);
