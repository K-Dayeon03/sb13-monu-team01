package com.project.monu.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.monu.global.dto.CursorPageResponse;
import com.project.monu.domain.notification.dto.NotificationConfirmAllResponse;
import com.project.monu.domain.notification.dto.NotificationResponse;
import com.project.monu.domain.notification.entity.Notification;
import com.project.monu.domain.notification.entity.NotificationResourceType;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.global.exception.BusinessException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    private final NotificationRepository notificationRepository =
            org.mockito.Mockito.mock(NotificationRepository.class);

    private final NotificationService notificationService =
            new NotificationService(notificationRepository);

    @Test
    void 본인_알림을_단건_확인할_수_있다() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification notification = createNotification(userId);

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        NotificationResponse response =
                notificationService.confirmNotification(notificationId, userId);

        assertThat(response.confirmed()).isTrue();
        assertThat(notification.isConfirmed()).isTrue();
        assertThat(notification.getUpdatedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_알림을_확인하면_예외가_발생한다() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.confirmNotification(notificationId, userId)
        ).isInstanceOf(BusinessException.class);
    }

    @Test
    void 다른_사용자의_알림은_확인할_수_없다() {
        UUID requestUserId = UUID.randomUUID();
        UUID notificationOwnerId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        Notification notification = createNotification(notificationOwnerId);

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        assertThatThrownBy(() ->
                notificationService.confirmNotification(notificationId, requestUserId)
        ).isInstanceOf(BusinessException.class);
    }

    private Notification createNotification(UUID userId) {
        return Notification.create(
                userId,
                "새로운 알림입니다.",
                NotificationResourceType.COMMENT,
                UUID.randomUUID()
        );
    }

    @Test
    void 사용자의_미확인_알림을_전체_확인할_수_있다() {
        UUID userId = UUID.randomUUID();
        Notification firstNotification = createNotification(userId);
        Notification secondNotification = createNotification(userId);

        when(notificationRepository.findByUserIdAndConfirmedFalse(userId))
                .thenReturn(List.of(firstNotification, secondNotification));

        NotificationConfirmAllResponse response =
                notificationService.confirmAllNotifications(userId);

        assertThat(response.confirmedCount()).isEqualTo(2);
        assertThat(firstNotification.isConfirmed()).isTrue();
        assertThat(secondNotification.isConfirmed()).isTrue();
        assertThat(firstNotification.getUpdatedAt()).isNotNull();
        assertThat(secondNotification.getUpdatedAt()).isNotNull();
    }

    @Test
    void 미확인_알림_목록을_조회할_수_있다() {
        UUID userId = UUID.randomUUID();
        Notification firstNotification = createNotification(userId);
        Notification secondNotification = createNotification(userId);

        when(notificationRepository.findByUserIdAndConfirmedFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(firstNotification, secondNotification));

        CursorPageResponse<NotificationResponse> response =
                notificationService.getNotifications(userId, 10);

        assertThat(response.content()).hasSize(2);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.hasNext()).isFalse();
    }

//    @Test
//    void 미확인_알림이_limit보다_많으면_hasNext가_true다() {
//        UUID userId = UUID.randomUUID();
//        Notification firstNotification = createNotification(userId);
//        Notification secondNotification = createNotification(userId);
//        Notification extraNotification = createNotification(userId);
//
//        when(notificationRepository.findByUserIdAndConfirmedFalseOrderByCreatedAtDesc(userId))
//                .thenReturn(List.of(firstNotification, secondNotification, extraNotification));
//
//        CursorPageResponse<NotificationResponse> response =
//                notificationService.getNotifications(userId, 2);
//
//        assertThat(response.content()).hasSize(2);
//        assertThat(response.size()).isEqualTo(2);
//        assertThat(response.totalElements()).isEqualTo(3);
//        assertThat(response.hasNext()).isTrue();
//    }

    @Test
    void limit이_0보다_작거나_같으면_기본값_10으로_조회한다() {
        UUID userId = UUID.randomUUID();

        when(notificationRepository.findByUserIdAndConfirmedFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        CursorPageResponse<NotificationResponse> response =
                notificationService.getNotifications(userId, 0);

        assertThat(response.content()).isEmpty();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isZero();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void limit이_100보다_크면_최대값_100으로_조회한다() {
        UUID userId = UUID.randomUUID();

        when(notificationRepository.findByUserIdAndConfirmedFalseOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        CursorPageResponse<NotificationResponse> response =
                notificationService.getNotifications(userId, 1000);

        assertThat(response.content()).isEmpty();
        assertThat(response.size()).isEqualTo(100);
        assertThat(response.totalElements()).isZero();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void 일주일_지난_확인_알림을_삭제한다() {
        Instant now = Instant.parse("2026-08-24T00:00:00Z");
        Instant expectedThreshold = Instant.parse("2026-08-17T00:00:00Z");

        when(notificationRepository.deleteByConfirmedTrueAndUpdatedAtBefore(expectedThreshold))
                .thenReturn(5L);

        long deletedCount = notificationService.deleteOldConfirmedNotifications(now);

        assertThat(deletedCount).isEqualTo(5L);
        verify(notificationRepository).deleteByConfirmedTrueAndUpdatedAtBefore(expectedThreshold);
    }
}