package com.civictransparency.complaint.entity;

import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.geography.entity.City;
import com.civictransparency.geography.entity.Ward;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "complaint", schema = "complaint")
public class Complaint {

    @Id
    @Column(name = "complaint_id", updatable = false, nullable = false)
    private UUID complaintId;

    @Column(name = "complaint_reference", unique = true, nullable = false, updatable = false, length = 30)
    private String complaintReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "citizen_id", nullable = false, updatable = false)
    private CitizenProfile citizen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, updatable = false)
    private ComplaintCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", updatable = false)
    private ComplaintSubcategory subcategory;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private Severity severity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false, updatable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", updatable = false)
    private Ward ward;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Complaint() {
        // JPA
    }

    public Complaint(UUID complaintId, String complaintReference, CitizenProfile citizen, ComplaintCategory category, ComplaintSubcategory subcategory, String title, String description, Severity severity, City city, Ward ward) {
        this.complaintId = complaintId;
        this.complaintReference = complaintReference;
        this.citizen = citizen;
        this.category = category;
        this.subcategory = subcategory;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.city = city;
        this.ward = ward;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getComplaintId() { return complaintId; }
    public String getComplaintReference() { return complaintReference; }
    public CitizenProfile getCitizen() { return citizen; }
    public ComplaintCategory getCategory() { return category; }
    public ComplaintSubcategory getSubcategory() { return subcategory; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Severity getSeverity() { return severity; }
    public City getCity() { return city; }
    public Ward getWard() { return ward; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
