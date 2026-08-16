package com.project.monu.domain.users.service;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserService userService = new UserService(userRepository);

  @Test
  void 정상적인_정보로_회원가입할_수_있다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "password123!"
    );

    User savedUser = User.builder()
        .email("test@test.com")
        .nickname("테스트")
        .password("password123!")
        .build();

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    UserResponse response = userService.create(request);

    assertThat(response.email()).isEqualTo("test@test.com");
    assertThat(response.nickname()).isEqualTo("테스트");

    verify(userRepository).save(any(User.class));
  }
}