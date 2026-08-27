package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.users.repository.UserRepository;

import java.util.UUID;

public class UserPhysicalDeleteService {

  private final UserRepository userRepository;
  private final ArticleViewRepository articleViewRepository;
  private final NotificationRepository notificationRepository;
  private final SubscriptionRepository subscriptionRepository;

  public UserPhysicalDeleteService(
      UserRepository userRepository,
      ArticleViewRepository articleViewRepository,
      NotificationRepository notificationRepository,
      SubscriptionRepository subscriptionRepository
  ) {
    this.userRepository = userRepository;
    this.articleViewRepository = articleViewRepository;
    this.notificationRepository = notificationRepository;
    this.subscriptionRepository = subscriptionRepository;
  }

  public void hardDelete(UUID userId) {
    articleViewRepository.deleteAllByViewer_Id(userId);
    notificationRepository.deleteAllByUserId(userId);
  }
}