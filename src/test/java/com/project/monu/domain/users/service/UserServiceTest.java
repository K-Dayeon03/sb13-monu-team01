package com.project.monu.domain.users.service;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

  @Test
  void 이미_존재하는_이메일로_회원가입할_수_없다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "새로운사용자",
        "password123!"
    );

    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("이미 존재하는 이메일입니다.");
  }

  // password test Encoder
  @Test
  void 회원가입_시_비밀번호가_암호화되어_저장된다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "password123!"
    );

    when(userRepository.save(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    userService.create(request);

    verify(userRepository).save(argThat(user ->
        !user.getPassword().equals(request.password())
    ));
  }
}