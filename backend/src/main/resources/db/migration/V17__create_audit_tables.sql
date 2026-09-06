CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE audit.audit_event (
    event_id        UUID PRIMARY KEY,
    event_type      VARCHAR(100) NOT NULL,
    actor_user_id   UUID,
    complaint_id    UUID,
    payload         JSONB,
    description     TEXT NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_complaint ON audit.audit_event (complaint_id);
CREATE INDEX idx_audit_actor     ON audit.audit_event (actor_user_id);
