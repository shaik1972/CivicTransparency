package com.civictransparency.assignment.entity;

import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.identity.entity.UserAccount;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaint_assignment", schema = "assignment")
public class ComplaintAssignment {

    @Id
    @Column(name = "assignment_id")
    private UUID assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private AuthorityDepartment department;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "unassigned_at")
    private OffsetDateTime unassignedAt;

    @Column(name = "assignment_reason", length = 50)
    private String assignmentReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private UserAccount assignedBy;

    protected ComplaintAssignment() {}

    public ComplaintAssignment(UUID assignmentId, Complaint complaint, AuthorityDepartment department, String assignmentReason, UserAccount assignedBy) {
        this.assignmentId = assignmentId;
        this.complaint = complaint;
        this.department = department;
        this.assignedAt = OffsetDateTime.now();
        this.assignmentReason = assignmentReason;
        this.assignedBy = assignedBy;
    }

    public void markUnassigned() {
        this.unassignedAt = OffsetDateTime.now();
    }

    public UUID getAssignmentId() { return assignmentId; }
    public Complaint getComplaint() { return complaint; }
    public AuthorityDepartment getDepartment() { return department; }
    public OffsetDateTime getAssignedAt() { return assignedAt; }
    public OffsetDateTime getUnassignedAt() { return unassignedAt; }
    public String getAssignmentReason() { return assignmentReason; }
    public UserAccount getAssignedBy() { return assignedBy; }
}
