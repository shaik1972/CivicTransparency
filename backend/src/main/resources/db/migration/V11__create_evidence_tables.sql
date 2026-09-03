CREATE TABLE evidence.evidence (
    evidence_id UUID PRIMARY KEY,
    complaint_id UUID NOT NULL,
    storage_key VARCHAR(500) UNIQUE NOT NULL,
    media_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    sha256 CHAR(64) NOT NULL,
    duration_seconds INTEGER,
    processing_status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_evidence_complaint FOREIGN KEY (complaint_id) REFERENCES complaint.complaint (complaint_id)
);

CREATE TABLE evidence.media_moderation (
    moderation_id UUID PRIMARY KEY,
    evidence_id UUID UNIQUE NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason_code VARCHAR(50),
    reviewed_by UUID,
    reviewed_at TIMESTAMPTZ,

    CONSTRAINT fk_media_moderation_evidence FOREIGN KEY (evidence_id) REFERENCES evidence.evidence (evidence_id),
    CONSTRAINT fk_media_moderation_reviewer FOREIGN KEY (reviewed_by) REFERENCES identity.user_account (user_id)
);

CREATE INDEX idx_evidence_complaint ON evidence.evidence (complaint_id);
