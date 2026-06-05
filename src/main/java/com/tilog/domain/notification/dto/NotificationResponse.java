package com.tilog.domain.notification.dto;

import com.tilog.domain.notification.entity.Notification;
import com.tilog.domain.notification.entity.NotificationType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private final Long notificationId;
    private final Long senderId;
    private final String senderNickname;
    private final NotificationType notificationType;
    private final String message;
    private final boolean isRead;
    private final Long relatedEntityId;
    private final String relatedEntityType;
    private final LocalDateTime createdAt;

    private NotificationResponse(Notification notification) {
        this.notificationId = notification.getId();
        this.senderId = notification.getSender() != null
                ? notification.getSender().getId() : null;
        this.senderNickname = notification.getSender() != null
                ? notification.getSender().getNickname() : null;
        this.notificationType = notification.getNotificationType();
        this.message = notification.getMessage();
        this.isRead = notification.isRead();
        this.relatedEntityId = notification.getRelatedEntityId();
        this.relatedEntityType = notification.getRelatedEntityType();
        this.createdAt = notification.getCreatedAt();
    }

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification);
    }
}
