package com.tilog.entity.notification;

import com.tilog.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    // 시스템 알림일 경우 NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(nullable = false, length = 300)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "related_entity_type", length = 30)
    private String relatedEntityType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Notification create(Member receiver, Member sender,
                                      NotificationType type, String message,
                                      Long relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification();
        notification.receiver = receiver;
        notification.sender = sender;
        notification.notificationType = type;
        notification.message = message;
        notification.relatedEntityId = relatedEntityId;
        notification.relatedEntityType = relatedEntityType;
        return notification;
    }

    public void read() {
        this.isRead = true;
    }
}
