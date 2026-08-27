package com.project.monu.domain.users.service;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.request.UserLoginRequest;
import com.project.monu.domain.users.dto.request.UserUpdateRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

class UserServiceTest {

  private final UserRepository userRepository = mock(UserRepository.class);
  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private final UserService userService = new UserService(userRepository, passwordEncoder);

  @Test
  void 정상적인_정보로_회원가입할_수_있다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "password123!"
    );

    when(passwordEncoder.encode(request.password()))
        .thenReturn("encoded-password");

    User savedUser = User.builder()
        .email("test@test.com")
        .nickname("테스트")
        .password("encoded-password")
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

    when(passwordEncoder.encode(request.password()))
        .thenReturn("encoded-password");

    when(userRepository.save(any(User.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    userService.create(request);

    verify(userRepository).save(argThat(user ->
        "encoded-password".equals(user.getPassword())
    ));
  }

  @Test
  void 회원가입_중_이메일_중복으로_데이터베이스_예외가_발생하면_이메일_중복_예외로_변환한다() {
    UserCreateRequest request = new UserCreateRequest(
        "test@test.com",
        "테스트",
        "password123!"
    );

    when(userRepository.existsByEmail(request.email()))
        .thenReturn(false);

    when(userRepository.save(any(User.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate email"));

    assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("이미 존재하는 이메일입니다.");
  }

  // login
  @Test
  void DB에_존재하는_사용자의_올바른_이메일과_비밀번호로_로그인할_수_있다() {
    User user = User.builder()
        .email("test@test.com")
        .nickname("테스트")
        .password("encoded-password")
        .build();

    UserLoginRequest request = new UserLoginRequest(
        "test@test.com",
        "password123!"
    );

    when(userRepository.findByEmail(request.email()))
        .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(
        request.password(),
        user.getPassword()
    )).thenReturn(true);

    UserResponse response = userService.login(request);

    assertThat(response.email()).isEqualTo("test@test.com");
    assertThat(response.nickname()).isEqualTo("테스트");
  }

  @Test
  void 존재하지_않는_이메일로_로그인하면_로그인에_실패한다() {
    UserLoginRequest request = new UserLoginRequest(
        "notfound@test.com",
        "password123!"
    );

    when(userRepository.findByEmail(request.email()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
  }

  @Test
  void 비밀번호가_틀리면_로그인에_실패한다() {
    User user = User.builder()
        .email("test@test.com")
        .nickname("테스트")
        .password("encoded-password")
        .build();

    UserLoginRequest request = new UserLoginRequest(
        "test@test.com",
        "wrong-password"
    );

    when(userRepository.findByEmail(request.email()))
        .thenReturn(Optional.of(user));

    when(passwordEncoder.matches(
        request.password(),
        user.getPassword()
    )).thenReturn(false);

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
  }

  // nickname 수정
  @Test
  void 사용자의_닉네임을_수정할_수_있다() {
    UUID userId = UUID.randomUUID();

    User user = User.builder()
        .email("test@test.com")
        .nickname("기존닉네임")
        .password("encoded-password")
        .build();

    UserUpdateRequest request = new UserUpdateRequest(
        "새닉네임"
    );

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user));

    when(userRepository.save(user))
        .thenReturn(user);

    UserResponse response = userService.update(userId, userId, request);

    assertThat(response.nickname()).isEqualTo("새닉네임");

    verify(userRepository).save(user);
  }

  @Test
  void 존재하지_않는_사용자의_닉네임은_수정할_수_없다() {
    UUID userId = UUID.randomUUID();

    UserUpdateRequest request = new UserUpdateRequest(
        "새닉네임"
    );

    when(userRepository.findById(userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> userService.update(userId, userId, request)
    )
        .isInstanceOf(BusinessException.class)
        .hasMessage("사용자를 찾을 수 없습니다.");
  }

  @Test
  void 다른_사용자의_닉네임은_수정할_수_없다() {
    UUID userId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    UserUpdateRequest request = new UserUpdateRequest(
        "새닉네임"
    );

    assertThatThrownBy(
        () -> userService.update(userId, requestUserId, request)
    )
        .isInstanceOf(BusinessException.class)
        .hasMessage("사용자 정보 수정 권한이 없습니다.");
  }

  // 논리 삭제
  @Test
  void 사용자가_자신의_계정을_논리_삭제할_수_있다() {
    UUID userId = UUID.randomUUID();

    User user = User.builder()
        .email("test@test.com")
        .nickname("테스트")
        .password("encoded-password")
        .build();

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user));

    when(userRepository.save(user))
        .thenReturn(user);

    userService.delete(userId, userId);

    assertThat(user.getDeletedAt()).isNotNull();
    verify(userRepository).save(user);
  }

  @Test
  void 존재하지_않는_사용자는_삭제할_수_없다() {
    UUID userId = UUID.randomUUID();

    when(userRepository.findById(userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> userService.delete(userId, userId)
    )
        .isInstanceOf(BusinessException.class)
        .hasMessage("사용자를 찾을 수 없습니다.");
  }

  @Test
  void 다른_사용자의_계정은_삭제할_수_없다() {
    UUID userId = UUID.randomUUID();
    UUID requestUserId = UUID.randomUUID();

    assertThatThrownBy(
        () -> userService.delete(userId, requestUserId)
    )
        .isInstanceOf(BusinessException.class)
        .hasMessage("사용자 삭제 권한이 없습니다.");
  }

  @Test
  void 논리_삭제된_사용자는_로그인할_수_없다() {
    User user = User.builder()
        .email("deleted@test.com")
        .nickname("삭제사용자")
        .password("encoded-password")
        .build();

    user.delete();

    UserLoginRequest request = new UserLoginRequest(
        "deleted@test.com",
        "password123!"
    );

    when(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.login(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("이메일 또는 비밀번호가 올바르지 않습니다.");
  }

  @Test
  void 이미_논리_삭제된_사용자는_다시_삭제할_수_없다() {
    UUID userId = UUID.randomUUID();

    User user = User.builder()
        .email("deleted@test.com")
        .nickname("삭제사용자")
        .password("encoded-password")
        .build();

    user.delete();

    when(userRepository.findByIdAndDeletedAtIsNull(userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
        () -> userService.delete(userId, userId)
    )
        .isInstanceOf(BusinessException.class)
        .hasMessage("사용자를 찾을 수 없습니다.");
  }
}