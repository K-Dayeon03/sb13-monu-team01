package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.users.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserPhysicalDeleteServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final ArticleViewRepository articleViewRepository = mock(ArticleViewRepository.class);
  private final NotificationRepository notificationRepository = mock(NotificationRepository.class);

  private final UserPhysicalDeleteService userPhysicalDeleteService =
      new UserPhysicalDeleteService(
          userRepository,
          articleViewRepository,
          notificationRepository
      );

  @Test
  void 사용자_물리_삭제시_최근_본_기사_기록을_삭제한다() {
    UUID userId = UUID.randomUUID();

    userPhysicalDeleteService.hardDelete(userId);

    verify(articleViewRepository).deleteAllByViewer_Id(userId);
  }

  @Test
  void 사용자_물리_삭제시_알림을_삭제한다() {
    UUID userId = UUID.randomUUID();

    userPhysicalDeleteService.hardDelete(userId);

    verify(notificationRepository).deleteAllByUserId(userId);
  }
}