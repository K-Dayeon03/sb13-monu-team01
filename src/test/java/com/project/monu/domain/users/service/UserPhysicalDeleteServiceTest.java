package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.users.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPhysicalDeleteServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final ArticleViewRepository articleViewRepository = mock(ArticleViewRepository.class);
  private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
  private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);

  private final UserPhysicalDeleteService userPhysicalDeleteService =
      new UserPhysicalDeleteService(
          userRepository,
          articleViewRepository,
          notificationRepository,
          subscriptionRepository
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

  @Test
  void 사용자_물리_삭제시_구독을_삭제하고_관심사_구독자수를_감소시킨다() {
    UUID userId = UUID.randomUUID();

    Interest interest = Interest.create("경제");
    Subscription subscription = Subscription.create(userId, interest);

    when(subscriptionRepository.findAllByUserId(userId))
        .thenReturn(List.of(subscription));

    userPhysicalDeleteService.hardDelete(userId);

    assertThat(interest.getSubscriberCount()).isEqualTo(0L);
    verify(subscriptionRepository).delete(subscription);
  }
}