package com.project.monu.domain.users.service;

import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserPhysicalDeleteSchedulerTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserPhysicalDeleteService userPhysicalDeleteService =
      mock(UserPhysicalDeleteService.class);

  private final UserPhysicalDeleteScheduler scheduler =
      new UserPhysicalDeleteScheduler(
          userRepository,
          userPhysicalDeleteService
      );

  @Test
  void 논리_삭제_후_24시간이_지난_사용자를_물리_삭제한다() {
    UUID userId = UUID.randomUUID();
    User user = mock(User.class);

    when(user.getId()).thenReturn(userId);
    when(userRepository.findAllByDeletedAtLessThanEqual(any(Instant.class)))
        .thenReturn(List.of(user));

    scheduler.deleteExpiredUsers();

    verify(userRepository).findAllByDeletedAtLessThanEqual(any(Instant.class));
    verify(userPhysicalDeleteService).hardDelete(userId);
  }
}