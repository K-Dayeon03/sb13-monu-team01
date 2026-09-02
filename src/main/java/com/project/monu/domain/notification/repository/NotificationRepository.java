package com.project.monu.domain.notification.repository;

import com.project.monu.domain.notification.entity.Notification;
import com.project.monu.domain.notification.entity.NotificationResourceType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdAndConfirmedFalseOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndConfirmedFalse(UUID userId);

    long deleteByConfirmedTrueAndUpdatedAtBefore(Instant threshold);

    void deleteAllByResourceTypeAndResourceIdIn(
            NotificationResourceType resourceType,
            List<UUID> resourceIds
    );

    // User 물리 삭제를 위한 추가
    void deleteAllByUserId(UUID userId);
}
