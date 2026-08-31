package com.project.monu.domain.notification.event;

import com.project.monu.domain.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CommentLikedEvent event) {
        try {
            notificationService.createCommentLikeNotification(
                    event.commentAuthorId(),
                    event.likedByUserId(),
                    event.likedByUserNickname(),
                    event.commentId()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "댓글 좋아요 알림 생성에 실패했습니다. commentAuthorId={}, likedByUserId={}, commentId={}",
                    event.commentAuthorId(),
                    event.likedByUserId(),
                    event.commentId(),
                    exception
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(InterestArticleCreatedEvent event) {
        try {
            notificationService.createInterestArticleNotifications(
                    event.interestId(),
                    event.interestName(),
                    event.articleCount(),
                    event.subscriberUserIds()
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "관심사 기사 등록 알림 생성에 실패했습니다. interestId={}, interestName={}, articleCount={}, subscriberCount={}",
                    event.interestId(),
                    event.interestName(),
                    event.articleCount(),
                    event.subscriberUserIds().size(),
                    exception
            );
        }
    }
}