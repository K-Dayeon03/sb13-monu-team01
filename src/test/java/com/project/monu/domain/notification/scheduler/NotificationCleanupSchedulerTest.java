package com.project.monu.domain.notification.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.monu.domain.notification.service.NotificationService;
import org.junit.jupiter.api.Test;

class NotificationCleanupSchedulerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final NotificationCleanupScheduler notificationCleanupScheduler =
            new NotificationCleanupScheduler(notificationService);

    @Test
    void 오래된_확인_알림_삭제를_실행한다() {
        when(notificationService.deleteOldConfirmedNotifications(any()))
                .thenReturn(3L);

        notificationCleanupScheduler.deleteOldConfirmedNotifications();

        verify(notificationService).deleteOldConfirmedNotifications(any());
    }
}