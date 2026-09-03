CREATE TABLE workflow.workflow (
    workflow_id UUID PRIMARY KEY,
    complaint_id UUID UNIQUE NOT NULL,
    current_state VARCHAR(30) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    version BIGINT NOT NULL,

    CONSTRAINT fk_workflow_complaint FOREIGN KEY (complaint_id) REFERENCES complaint.complaint (complaint_id)
);

CREATE TABLE workflow.workflow_transition (
    transition_id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    from_state VARCHAR(30),
    to_state VARCHAR(30) NOT NULL,
    reason TEXT,
    actor_user_id UUID,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_transition_workflow FOREIGN KEY (workflow_id) REFERENCES workflow.workflow (workflow_id),
    CONSTRAINT fk_transition_actor FOREIGN KEY (actor_user_id) REFERENCES identity.user_account (user_id)
);

CREATE INDEX idx_workflow_current_state ON workflow.workflow (current_state);
CREATE INDEX idx_workflow_current_state_started ON workflow.workflow (current_state, started_at);
