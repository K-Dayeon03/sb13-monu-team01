package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.comment.service.CommentService;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.useractivity.repository.UserActivityMongoRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPhysicalDeleteService {

  private final UserRepository userRepository;
  private final ArticleViewRepository articleViewRepository;
  private final NotificationRepository notificationRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final UserActivityMongoRepository userActivityMongoRepository;
  private final CommentService commentService;

  public UserPhysicalDeleteService(
      UserRepository userRepository,
      ArticleViewRepository articleViewRepository,
      NotificationRepository notificationRepository,
      SubscriptionRepository subscriptionRepository,
      UserActivityMongoRepository userActivityMongoRepository,
      CommentService commentService
  ) {
    this.userRepository = userRepository;
    this.articleViewRepository = articleViewRepository;
    this.notificationRepository = notificationRepository;
    this.subscriptionRepository = subscriptionRepository;
    this.userActivityMongoRepository = userActivityMongoRepository;
    this.commentService = commentService;
  }

  // /hard API에서 사용
  @Transactional
  public void hardDelete(UUID userId, UUID requestUserId) {
    if (!userId.equals(requestUserId)) {
      throw new BusinessException(ErrorCode.USER_DELETE_ACCESS_DENIED);
    }

    hardDelete(userId);
  }

  // 실제 물리 삭제 로직
  // 24시간 경과 Batch에서도 이 메서드를 호출
  @Transactional
  public void hardDelete(UUID userId) {
    articleViewRepository.deleteAllByViewer_Id(userId);
    notificationRepository.deleteAllByUserId(userId);

    List<Subscription> subscriptions =
        subscriptionRepository.findAllByUserId(userId);

    for (Subscription subscription : subscriptions) {
      subscription.getInterest().decreaseSubscriberCount();
      subscriptionRepository.delete(subscription);
    }

    userActivityMongoRepository.deleteById(userId);
    commentService.hardDeleteAllByUserId(userId);

    User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    userRepository.delete(user);
  }
}