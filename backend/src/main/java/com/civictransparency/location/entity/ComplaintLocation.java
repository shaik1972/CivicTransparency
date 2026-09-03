package com.civictransparency.location.entity;

import com.civictransparency.complaint.entity.Complaint;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "complaint_location", schema = "location")
public class ComplaintLocation {

    @Id
    @Column(name = "location_id", updatable = false, nullable = false)
    private UUID locationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", unique = true, nullable = false, updatable = false)
    private Complaint complaint;

    @Column(name = "point", columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point point;

    @Column(name = "accuracy_meters", precision = 10, scale = 2)
    private BigDecimal accuracyMeters;

    @Column(name = "location_description", columnDefinition = "TEXT")
    private String locationDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "exposure_policy", nullable = false, length = 20)
    private ExposurePolicy exposurePolicy;

    @Column(name = "public_geometry", columnDefinition = "GEOMETRY")
    private Geometry publicGeometry;

    protected ComplaintLocation() {
    }

    public ComplaintLocation(UUID locationId, Complaint complaint, Point point, BigDecimal accuracyMeters, String locationDescription, ExposurePolicy exposurePolicy, Geometry publicGeometry) {
        this.locationId = locationId;
        this.complaint = complaint;
        this.point = point;
        this.accuracyMeters = accuracyMeters;
        this.locationDescription = locationDescription;
        this.exposurePolicy = exposurePolicy;
        this.publicGeometry = publicGeometry;
    }

    public UUID getLocationId() { return locationId; }
    public Complaint getComplaint() { return complaint; }
    public Point getPoint() { return point; }
    public BigDecimal getAccuracyMeters() { return accuracyMeters; }
    public String getLocationDescription() { return locationDescription; }
    public ExposurePolicy getExposurePolicy() { return exposurePolicy; }
    public Geometry getPublicGeometry() { return publicGeometry; }
}
