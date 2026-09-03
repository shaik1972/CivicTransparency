package com.civictransparency.complaint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "complaint_subcategory", schema = "complaint")
public class ComplaintSubcategory {

    @Id
    @Column(name = "subcategory_id", nullable = false)
    private UUID subcategoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ComplaintCategory category;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "default_priority", nullable = false, length = 20)
    private String defaultPriority;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected ComplaintSubcategory() {
        // Required by JPA
    }

    public UUID getSubcategoryId() {
        return subcategoryId;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultPriority() {
        return defaultPriority;
    }

    public boolean isActive() {
        return active;
    }
}
