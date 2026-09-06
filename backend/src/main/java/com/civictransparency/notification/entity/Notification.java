package com.civictransparency.notification.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification", schema = "notification")
public class Notification {

    @Id
    @Column(name = "notification_id")
    private UUID notificationId;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Notification() {}

    public Notification(UUID notificationId, UUID recipientUserId, String category, String title, String body) {
        this.notificationId = notificationId;
        this.recipientUserId = recipientUserId;
        this.category = category;
        this.title = title;
        this.body = body;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID getNotificationId() { return notificationId; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public OffsetDateTime getReadAt() { return readAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
