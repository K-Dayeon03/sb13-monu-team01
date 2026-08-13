package com.project.monu.domain.notification.dto;

import com.project.monu.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID userId,
        String content,
        String resourceType,
        UUID resourceId,
        boolean confirmed
) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getUserId(),
                notification.getContent(),
                notification.getResourceType().name().toLowerCase(),
                notification.getResourceId(),
                notification.isConfirmed()
        );
    }
}