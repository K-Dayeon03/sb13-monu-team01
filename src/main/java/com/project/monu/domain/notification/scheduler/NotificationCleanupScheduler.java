package com.project.monu.domain.notification.scheduler;

import com.project.monu.domain.notification.service.NotificationService;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationCleanupScheduler {

    private final NotificationService notificationService;

    public NotificationCleanupScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldConfirmedNotifications() {
        notificationService.deleteOldConfirmedNotifications(Instant.now());
    }
}