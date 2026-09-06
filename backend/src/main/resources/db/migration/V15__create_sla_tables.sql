CREATE SCHEMA IF NOT EXISTS sla;

CREATE TABLE sla.sla_policy (
    policy_id           UUID PRIMARY KEY,
    category_id         UUID NOT NULL,
    subcategory_id      UUID,
    severity            VARCHAR(20) NOT NULL,
    target_hours        INTEGER NOT NULL,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE sla.complaint_sla (
    sla_id              UUID PRIMARY KEY,
    complaint_id        UUID NOT NULL,
    policy_id           UUID NOT NULL,
    deadline            TIMESTAMP WITH TIME ZONE NOT NULL,
    is_paused           BOOLEAN NOT NULL DEFAULT FALSE,
    paused_at           TIMESTAMP WITH TIME ZONE,
    paused_reason       VARCHAR(200),
    resolved_at         TIMESTAMP WITH TIME ZONE,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_sla_complaint FOREIGN KEY (complaint_id) REFERENCES complaint.complaint (complaint_id),
    CONSTRAINT fk_sla_policy    FOREIGN KEY (policy_id)    REFERENCES sla.sla_policy (policy_id),
    CONSTRAINT uq_complaint_sla UNIQUE (complaint_id)
);
