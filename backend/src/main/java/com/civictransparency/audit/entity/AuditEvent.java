package com.civictransparency.audit.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_event", schema = "audit")
public class AuditEvent {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "complaint_id")
    private UUID complaintId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected AuditEvent() {
    }

    public AuditEvent(UUID eventId, String eventType, UUID actorUserId, UUID complaintId, String description,
            String payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.actorUserId = actorUserId;
        this.complaintId = complaintId;
        this.description = description;
        this.payload = payload;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public UUID getComplaintId() {
        return complaintId;
    }

    public String getPayload() {
        return payload;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
