package com.civictransparency.notification.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_delivery", schema = "notification")
public class NotificationDelivery {

    @Id
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "attempted_at")
    private OffsetDateTime attemptedAt;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "failure_reason")
    private String failureReason;

    protected NotificationDelivery() {}

    public NotificationDelivery(UUID deliveryId, Notification notification, String channel) {
        this.deliveryId = deliveryId;
        this.notification = notification;
        this.channel = channel;
        this.status = "PENDING";
    }

    public void markDelivered() {
        this.status = "DELIVERED";
        this.deliveredAt = OffsetDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = "FAILED";
        this.failureReason = reason;
    }

    public UUID getDeliveryId() { return deliveryId; }
    public Notification getNotification() { return notification; }
    public String getChannel() { return channel; }
    public String getStatus() { return status; }
}
