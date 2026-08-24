package com.project.monu.domain.notification.event;

import com.project.monu.domain.notification.service.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CommentLikedEvent event) {
        notificationService.createCommentLikeNotification(
                event.commentAuthorId(),
                event.likedByUserId(),
                event.likedByUserNickname(),
                event.commentId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(InterestArticleCreatedEvent event) {
        notificationService.createInterestArticleNotifications(
                event.interestId(),
                event.interestName(),
                event.articleCount(),
                event.subscriberUserIds()
        );
    }
}