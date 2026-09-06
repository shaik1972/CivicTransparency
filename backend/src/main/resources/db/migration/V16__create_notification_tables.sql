CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE notification.notification (
    notification_id     UUID PRIMARY KEY,
    recipient_user_id   UUID NOT NULL,
    category            VARCHAR(50)  NOT NULL,
    title               VARCHAR(255) NOT NULL,
    body                TEXT         NOT NULL,
    read_at             TIMESTAMP WITH TIME ZONE,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE notification.notification_delivery (
    delivery_id         UUID PRIMARY KEY,
    notification_id     UUID NOT NULL,
    channel             VARCHAR(20) NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempted_at        TIMESTAMP WITH TIME ZONE,
    delivered_at        TIMESTAMP WITH TIME ZONE,
    failure_reason      TEXT,
    CONSTRAINT fk_delivery_notification FOREIGN KEY (notification_id) REFERENCES notification.notification (notification_id)
);
