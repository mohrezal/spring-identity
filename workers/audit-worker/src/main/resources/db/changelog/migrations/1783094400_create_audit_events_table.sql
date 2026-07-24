-- liquibase formatted sql

-- changeset mohammadreza:1783094400
CREATE TABLE audit_events (
    id                UUID PRIMARY KEY,
    schema_version    INTEGER NOT NULL,
    event_id          VARCHAR(255) NOT NULL,
    event_type        VARCHAR(50) NOT NULL,
    outcome           VARCHAR(50) NOT NULL,
    occurred_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    trace_id          VARCHAR(255) NOT NULL,
    actor_user_id     UUID,
    subject_user_id   UUID,
    subject_email     VARCHAR(255),
    session_id        VARCHAR(255),
    request_id        VARCHAR(255),
    client_request_id VARCHAR(255),
    ip_address        VARCHAR(45),
    user_agent        VARCHAR(500),
    forwarded_host    VARCHAR(255),
    forwarded_proto   VARCHAR(10),
    reason            VARCHAR(1000),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_audit_events_event_id ON audit_events (event_id);
CREATE INDEX idx_audit_events_event_type ON audit_events (event_type);
CREATE INDEX idx_audit_events_occurred_at ON audit_events (occurred_at);
CREATE INDEX idx_audit_events_trace_id ON audit_events (trace_id);
CREATE INDEX idx_audit_events_actor_user_id ON audit_events (actor_user_id);
CREATE INDEX idx_audit_events_subject_user_id ON audit_events (subject_user_id);