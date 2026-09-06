package com.civictransparency.routing.entity;

import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.complaint.entity.Complaint;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "routing_decision", schema = "routing")
public class RoutingDecision {

    @Id
    @Column(name = "decision_id")
    private UUID decisionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RoutingRule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private AuthorityDepartment department;

    @Column(name = "specificity_score", nullable = false)
    private Integer specificityScore;

    @Column(name = "decided_at", nullable = false)
    private OffsetDateTime decidedAt;

    protected RoutingDecision() {}

    public RoutingDecision(UUID decisionId, Complaint complaint, RoutingRule rule, AuthorityDepartment department, Integer specificityScore) {
        this.decisionId = decisionId;
        this.complaint = complaint;
        this.rule = rule;
        this.department = department;
        this.specificityScore = specificityScore;
        this.decidedAt = OffsetDateTime.now();
    }

    public UUID getDecisionId() { return decisionId; }
    public Complaint getComplaint() { return complaint; }
    public RoutingRule getRule() { return rule; }
    public AuthorityDepartment getDepartment() { return department; }
    public Integer getSpecificityScore() { return specificityScore; }
    public OffsetDateTime getDecidedAt() { return decidedAt; }
}
