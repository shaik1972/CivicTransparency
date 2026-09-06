package com.civictransparency.sla.entity;

import com.civictransparency.complaint.entity.Complaint;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaint_sla", schema = "sla")
public class ComplaintSla {

    @Id
    @Column(name = "sla_id")
    private UUID slaId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false, unique = true)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private SlaPolicy policy;

    @Column(name = "deadline", nullable = false)
    private OffsetDateTime deadline;

    @Column(name = "is_paused", nullable = false)
    private Boolean isPaused = false;

    @Column(name = "paused_at")
    private OffsetDateTime pausedAt;

    @Column(name = "paused_reason", length = 200)
    private String pausedReason;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ComplaintSla() {}

    public ComplaintSla(UUID slaId, Complaint complaint, SlaPolicy policy, OffsetDateTime deadline) {
        this.slaId = slaId;
        this.complaint = complaint;
        this.policy = policy;
        this.deadline = deadline;
        this.isPaused = false;
        this.status = "ACTIVE";
        this.createdAt = OffsetDateTime.now();
    }

    public void pause(String reason) {
        this.isPaused = true;
        this.pausedAt = OffsetDateTime.now();
        this.pausedReason = reason;
    }

    public void resolve() {
        this.resolvedAt = OffsetDateTime.now();
        this.status = "RESOLVED";
    }

    public UUID getSlaId() { return slaId; }
    public Complaint getComplaint() { return complaint; }
    public SlaPolicy getPolicy() { return policy; }
    public OffsetDateTime getDeadline() { return deadline; }
    public Boolean getIsPaused() { return isPaused; }
    public String getStatus() { return status; }
    public OffsetDateTime getResolvedAt() { return resolvedAt; }
}
