package com.project.monu.domain.users.service;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

  private final UserService userService = new UserService();

  @Test
  void 정상적인_정보로_회원가입할_수_있다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "password123!"
    );

    UserResponse response = userService.create(request);

    assertThat(response.email()).isEqualTo("test@test.com");
    assertThat(response.nickname()).isEqualTo("테스트");
  }
}