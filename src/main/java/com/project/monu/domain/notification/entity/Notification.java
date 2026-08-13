package com.project.monu.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private NotificationResourceType resourceType;

    @Column(name = "resource_id", columnDefinition = "uuid", nullable = false)
    private UUID resourceId;

    @Column(nullable = false)
    private boolean confirmed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    private Notification(
            UUID userId,
            String content,
            NotificationResourceType resourceType,
            UUID resourceId
    ) {
        this.userId = userId;
        this.content = content;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public static Notification create(
            UUID userId,
            String content,
            NotificationResourceType resourceType,
            UUID resourceId
    ) {
        return new Notification(userId, content, resourceType, resourceId);
    }

    public void confirm(Instant confirmedAt) {
        if (confirmed) {
            return;
        }

        this.confirmed = true;
        this.updatedAt = confirmedAt;
    }
}