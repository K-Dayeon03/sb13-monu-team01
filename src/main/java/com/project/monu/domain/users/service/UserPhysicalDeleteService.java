package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.users.repository.UserRepository;

import java.util.UUID;

public class UserPhysicalDeleteService {

  private final UserRepository userRepository;
  private final ArticleViewRepository articleViewRepository;
  private final NotificationRepository notificationRepository;

  public UserPhysicalDeleteService(
      UserRepository userRepository,
      ArticleViewRepository articleViewRepository,
      NotificationRepository notificationRepository
  ) {
    this.userRepository = userRepository;
    this.articleViewRepository = articleViewRepository;
    this.notificationRepository = notificationRepository;
  }

  public void hardDelete(UUID userId) {
    articleViewRepository.deleteAllByViewer_Id(userId);
  }
}