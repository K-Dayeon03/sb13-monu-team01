package com.project.monu.domain.notification.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @Test
    void confirm을_호출하면_알림이_확인된다() {
        Notification notification = createNotification();

        notification.confirm(Instant.parse("2026-08-20T00:00:00Z"));

        assertThat(notification.isConfirmed()).isTrue();
    }

    @Test
    void confirm을_호출하면_확인_시간이_저장된다() {
        Notification notification = createNotification();
        Instant confirmedAt = Instant.parse("2026-08-20T00:00:00Z");

        notification.confirm(confirmedAt);

        assertThat(notification.getUpdatedAt()).isEqualTo(confirmedAt);
    }

    @Test
    void 이미_확인된_알림을_다시_confirm하면_기존_확인_시간을_유지한다() {
        Notification notification = createNotification();
        Instant firstConfirmedAt = Instant.parse("2026-08-20T00:00:00Z");
        Instant secondConfirmedAt = Instant.parse("2026-08-21T00:00:00Z");

        notification.confirm(firstConfirmedAt);
        notification.confirm(secondConfirmedAt);

        assertThat(notification.getUpdatedAt()).isEqualTo(firstConfirmedAt);
    }

    private Notification createNotification() {
        return Notification.create(
                UUID.randomUUID(),
                "새로운 알림입니다.",
                NotificationResourceType.COMMENT,
                UUID.randomUUID()
        );
    }
}