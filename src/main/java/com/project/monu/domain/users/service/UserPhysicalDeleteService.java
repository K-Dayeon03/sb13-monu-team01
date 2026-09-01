package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.useractivity.repository.UserActivityMongoRepository;
import com.project.monu.domain.users.repository.UserRepository;

import java.util.List;
import java.util.UUID;

public class UserPhysicalDeleteService {

  private final UserRepository userRepository;
  private final ArticleViewRepository articleViewRepository;
  private final NotificationRepository notificationRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final UserActivityMongoRepository userActivityMongoRepository;

  public UserPhysicalDeleteService(
      UserRepository userRepository,
      ArticleViewRepository articleViewRepository,
      NotificationRepository notificationRepository,
      SubscriptionRepository subscriptionRepository,
      UserActivityMongoRepository userActivityMongoRepository
  ) {
    this.userRepository = userRepository;
    this.articleViewRepository = articleViewRepository;
    this.notificationRepository = notificationRepository;
    this.subscriptionRepository = subscriptionRepository;
    this.userActivityMongoRepository = userActivityMongoRepository;
  }

  public void hardDelete(UUID userId) {
    articleViewRepository.deleteAllByViewer_Id(userId);
    notificationRepository.deleteAllByUserId(userId);

    List<Subscription> subscriptions = subscriptionRepository.findAllByUserId(userId);

    for (Subscription subscription : subscriptions) {
      subscription.getInterest().decreaseSubscriberCount();
      subscriptionRepository.delete(subscription);
    }
  }
}