package com.project.monu.domain.notification.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.project.monu.domain.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationEventListenerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final NotificationEventListener notificationEventListener =
            new NotificationEventListener(notificationService);

    @Test
    void 댓글_좋아요_이벤트를_받으면_댓글_좋아요_알림을_생성한다() {
        UUID commentAuthorId = UUID.randomUUID();
        UUID likedByUserId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        CommentLikedEvent event = new CommentLikedEvent(
                commentAuthorId,
                likedByUserId,
                "김모뉴",
                commentId
        );

        notificationEventListener.handle(event);

        verify(notificationService).createCommentLikeNotification(
                commentAuthorId,
                likedByUserId,
                "김모뉴",
                commentId
        );
    }

    @Test
    void 관심사_기사_등록_이벤트를_받으면_관심사_기사_등록_알림을_생성한다() {
        UUID interestId = UUID.randomUUID();
        UUID firstSubscriberId = UUID.randomUUID();
        UUID secondSubscriberId = UUID.randomUUID();

        InterestArticleCreatedEvent event = new InterestArticleCreatedEvent(
                interestId,
                "인공지능",
                3,
                List.of(firstSubscriberId, secondSubscriberId)
        );

        notificationEventListener.handle(event);

        verify(notificationService).createInterestArticleNotifications(
                interestId,
                "인공지능",
                3,
                List.of(firstSubscriberId, secondSubscriberId)
        );
    }

    @Test
    void 댓글_좋아요_알림_생성_실패는_리스너_밖으로_전파하지_않는다() {
        UUID commentAuthorId = UUID.randomUUID();
        UUID likedByUserId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        CommentLikedEvent event = new CommentLikedEvent(
                commentAuthorId,
                likedByUserId,
                "김모뉴",
                commentId
        );

        doThrow(new RuntimeException("알림 저장 실패"))
                .when(notificationService)
                .createCommentLikeNotification(
                        commentAuthorId,
                        likedByUserId,
                        "김모뉴",
                        commentId
                );

        assertThatCode(() -> notificationEventListener.handle(event))
                .doesNotThrowAnyException();

        verify(notificationService).createCommentLikeNotification(
                commentAuthorId,
                likedByUserId,
                "김모뉴",
                commentId
        );
    }

    @Test
    void 관심사_기사_알림_생성_실패는_리스너_밖으로_전파하지_않는다() {
        UUID interestId = UUID.randomUUID();
        UUID firstSubscriberId = UUID.randomUUID();
        UUID secondSubscriberId = UUID.randomUUID();
        List<UUID> subscriberUserIds = List.of(firstSubscriberId, secondSubscriberId);

        InterestArticleCreatedEvent event = new InterestArticleCreatedEvent(
                interestId,
                "인공지능",
                3,
                subscriberUserIds
        );

        doThrow(new RuntimeException("알림 저장 실패"))
                .when(notificationService)
                .createInterestArticleNotifications(
                        interestId,
                        "인공지능",
                        3,
                        subscriberUserIds
                );

        assertThatCode(() -> notificationEventListener.handle(event))
                .doesNotThrowAnyException();

        verify(notificationService).createInterestArticleNotifications(
                interestId,
                "인공지능",
                3,
                subscriberUserIds
        );
    }
}