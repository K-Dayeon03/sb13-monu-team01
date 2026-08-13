package com.project.monu.domain.notification.repository;

import com.project.monu.domain.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdAndConfirmedFalseOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUserIdAndConfirmedFalse(UUID userId);

    long deleteByConfirmedTrueAndUpdatedAtBefore(Instant threshold);
}