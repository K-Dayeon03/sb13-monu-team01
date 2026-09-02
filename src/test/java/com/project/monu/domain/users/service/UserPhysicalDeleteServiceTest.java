package com.project.monu.domain.users.service;

import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.comment.service.CommentService;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.useractivity.repository.UserActivityMongoRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPhysicalDeleteServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final ArticleViewRepository articleViewRepository = mock(ArticleViewRepository.class);
  private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
  private final SubscriptionRepository subscriptionRepository = mock(SubscriptionRepository.class);
  private final UserActivityMongoRepository userActivityMongoRepository = mock(UserActivityMongoRepository.class);
  private final CommentService commentService = mock(CommentService.class);

  private final UserPhysicalDeleteService userPhysicalDeleteService =
      new UserPhysicalDeleteService(
          userRepository,
          articleViewRepository,
          notificationRepository,
          subscriptionRepository,
          userActivityMongoRepository,
          commentService
      );

  @Test
  void 사용자_물리_삭제시_최근_본_기사_기록을_삭제한다() {
    UUID userId = UUID.randomUUID();
    givenUserExists(userId);

    userPhysicalDeleteService.hardDelete(userId);

    verify(articleViewRepository).deleteAllByViewer_Id(userId);
  }

  @Test
  void 사용자_물리_삭제시_알림을_삭제한다() {
    UUID userId = UUID.randomUUID();
    givenUserExists(userId);

    userPhysicalDeleteService.hardDelete(userId);

    verify(notificationRepository).deleteAllByUserId(userId);
  }

  @Test
  void 사용자_물리_삭제시_구독을_삭제하고_관심사_구독자수를_감소시킨다() {
    UUID userId = UUID.randomUUID();
    givenUserExists(userId);

    Interest interest = Interest.create("경제");
    Subscription subscription = Subscription.create(userId, interest);

    when(subscriptionRepository.findAllByUserId(userId))
        .thenReturn(List.of(subscription));

    userPhysicalDeleteService.hardDelete(userId);

    assertThat(interest.getSubscriberCount()).isEqualTo(0L);
    verify(subscriptionRepository).delete(subscription);
  }

  @Test
  void 사용자_물리_삭제시_MongoDB_활동내역을_삭제한다() {
    UUID userId = UUID.randomUUID();
    givenUserExists(userId);

    userPhysicalDeleteService.hardDelete(userId);

    verify(userActivityMongoRepository).deleteById(userId);
  }

  @Test
  void 사용자_물리_삭제시_댓글과_댓글좋아요를_삭제한다() {
    UUID userId = UUID.randomUUID();
    givenUserExists(userId);

    userPhysicalDeleteService.hardDelete(userId);

    verify(commentService).hardDeleteAllByUserId(userId);
  }

  @Test
  void 사용자_물리_삭제시_User를_실제로_삭제한다() {
    UUID userId = UUID.randomUUID();

    User user = mock(User.class);

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user));

    userPhysicalDeleteService.hardDelete(userId);

    verify(userRepository).delete(user);
  }

  @Test
  void 사용자_물리_삭제시_요청자와_삭제대상이_다르면_삭제에_실패한다() {
    UUID userId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    assertThatThrownBy(() ->
        userPhysicalDeleteService.hardDelete(userId, requestUserId)
    )
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_DELETE_ACCESS_DENIED);
  }

  @Test
  void 사용자_물리_삭제시_사용자정보가_없으면_삭제에_실패한다() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        userPhysicalDeleteService.hardDelete(userId, userId)
    )
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  private void givenUserExists(UUID userId) {
    User user = mock(User.class);

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user));
  }
}