package com.civictransparency.sla.entity;

import com.civictransparency.complaint.entity.ComplaintCategory;
import com.civictransparency.complaint.entity.ComplaintSubcategory;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "sla_policy", schema = "sla")
public class SlaPolicy {

    @Id
    @Column(name = "policy_id")
    private UUID policyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ComplaintCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private ComplaintSubcategory subcategory;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity;

    @Column(name = "target_hours", nullable = false)
    private Integer targetHours;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    protected SlaPolicy() {
    }

    public SlaPolicy(UUID policyId, ComplaintCategory category, ComplaintSubcategory subcategory,
            String severity, Integer targetHours, Boolean isActive) {
        this.policyId = policyId;
        this.category = category;
        this.subcategory = subcategory;
        this.severity = severity;
        this.targetHours = targetHours;
        this.isActive = isActive;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public ComplaintSubcategory getSubcategory() {
        return subcategory;
    }

    public String getSeverity() {
        return severity;
    }

    public Integer getTargetHours() {
        return targetHours;
    }

    public Boolean getIsActive() {
        return isActive;
    }
}
