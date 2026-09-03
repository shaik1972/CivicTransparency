package com.civictransparency.workflow.entity;

import com.civictransparency.identity.entity.UserAccount;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "workflow_transition", schema = "workflow")
public class WorkflowTransition {

    @Id
    @Column(name = "transition_id", updatable = false, nullable = false)
    private UUID transitionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false, updatable = false)
    private Workflow workflow;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", length = 30)
    private ComplaintState fromState;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 30)
    private ComplaintState toState;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", updatable = false)
    private UserAccount actor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected WorkflowTransition() {
    }

    public WorkflowTransition(UUID transitionId, Workflow workflow, ComplaintState fromState, ComplaintState toState, String reason, UserAccount actor) {
        this.transitionId = transitionId;
        this.workflow = workflow;
        this.fromState = fromState;
        this.toState = toState;
        this.reason = reason;
        this.actor = actor;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getTransitionId() { return transitionId; }
    public Workflow getWorkflow() { return workflow; }
    public ComplaintState getFromState() { return fromState; }
    public ComplaintState getToState() { return toState; }
    public String getReason() { return reason; }
    public UserAccount getActor() { return actor; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
