package com.civictransparency.routing.entity;

import com.civictransparency.authority.entity.AuthorityDepartment;
import com.civictransparency.complaint.entity.ComplaintCategory;
import com.civictransparency.complaint.entity.ComplaintSubcategory;
import com.civictransparency.geography.entity.City;
import com.civictransparency.geography.entity.State;
import com.civictransparency.geography.entity.Ward;
import com.civictransparency.complaint.entity.Severity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "routing_rule", schema = "routing")
public class RoutingRule {

    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ComplaintCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private ComplaintSubcategory subcategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 30)
    private Severity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private AuthorityDepartment department;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "effective_from")
    private OffsetDateTime effectiveFrom;

    @Column(name = "effective_to")
    private OffsetDateTime effectiveTo;

    protected RoutingRule() {}

    public UUID getRuleId() { return ruleId; }
    public String getName() { return name; }
    public State getState() { return state; }
    public City getCity() { return city; }
    public Ward getWard() { return ward; }
    public ComplaintCategory getCategory() { return category; }
    public ComplaintSubcategory getSubcategory() { return subcategory; }
    public Severity getSeverity() { return severity; }
    public AuthorityDepartment getDepartment() { return department; }
    public Integer getPriority() { return priority; }
    public Boolean getIsActive() { return isActive; }
    public OffsetDateTime getEffectiveFrom() { return effectiveFrom; }
    public OffsetDateTime getEffectiveTo() { return effectiveTo; }
}
