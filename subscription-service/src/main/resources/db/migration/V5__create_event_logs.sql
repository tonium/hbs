CREATE TABLE event_logs (
    id           BIGSERIAL   PRIMARY KEY,
    event_id     UUID        NOT NULL,
    trace_id     VARCHAR(64) NOT NULL,
    span_id      VARCHAR(32),
    event_type   VARCHAR(64) NOT NULL,
    service_name VARCHAR(64) NOT NULL,
    org_id       VARCHAR(64),
    actor_type   VARCHAR(32),
    actor_id     VARCHAR(128),
    target_type  VARCHAR(32),
    target_id    VARCHAR(128),
    status       VARCHAR(16) NOT NULL,
    payload      JSONB,
    occurred_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_event_logs_trace_id
    ON event_logs (trace_id);

CREATE INDEX ix_event_logs_target
    ON event_logs (target_type, target_id);

CREATE INDEX ix_event_logs_event_type_time
    ON event_logs (event_type, occurred_at DESC);

CREATE INDEX ix_event_logs_service_time
    ON event_logs (service_name, occurred_at DESC);
