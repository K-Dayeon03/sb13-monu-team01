package com.project.monu.domain.users.repository;

import com.project.monu.domain.users.entity.User;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  void 삭제되지_않은_사용자만_존재한다고_판단한다() {
    // given
    User activeUser = userRepository.save(user("active@test.com", "active"));
    User deletedUser = userRepository.save(user("deleted@test.com", "deleted"));
    ReflectionTestUtils.setField(deletedUser, "deletedAt", Instant.parse("2026-08-20T00:00:00Z"));
    userRepository.flush();

    // when & then
    assertThat(userRepository.existsByIdAndDeletedAtIsNull(activeUser.getId())).isTrue();
    assertThat(userRepository.existsByIdAndDeletedAtIsNull(deletedUser.getId())).isFalse();
  }

  private User user(String email, String nickname) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password("encoded-password")
        .build();
  }
}
