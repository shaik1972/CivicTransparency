package com.civictransparency.workflow.entity;

import com.civictransparency.complaint.entity.Complaint;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow", schema = "workflow")
public class Workflow {

    @Id
    @Column(name = "workflow_id", updatable = false, nullable = false)
    private UUID workflowId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", unique = true, nullable = false, updatable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false, length = 30)
    private ComplaintState currentState;

    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Workflow() {
    }

    public Workflow(UUID workflowId, Complaint complaint, ComplaintState initialState) {
        this.workflowId = workflowId;
        this.complaint = complaint;
        this.currentState = initialState;
        this.startedAt = OffsetDateTime.now();
    }

    public void updateState(ComplaintState newState) {
        this.currentState = newState;
        if (newState == ComplaintState.RESOLVED || newState == ComplaintState.REJECTED || newState == ComplaintState.CLOSED) {
            this.completedAt = OffsetDateTime.now();
        } else {
            this.completedAt = null;
        }
    }

    public UUID getWorkflowId() { return workflowId; }
    public Complaint getComplaint() { return complaint; }
    public ComplaintState getCurrentState() { return currentState; }
    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public Long getVersion() { return version; }
}
