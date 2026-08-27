package com.project.monu.domain.users.service;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.request.UserLoginRequest;
import com.project.monu.domain.users.dto.request.UserUpdateRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserResponse create(UserCreateRequest request) {

    if (userRepository.existsByEmail(request.email())) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(encodedPassword)
        .build();

    try {
      User savedUser = userRepository.save(user);

      return UserResponse.from(savedUser);
    } catch (DataIntegrityViolationException exception) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
  }

  public UserResponse login(UserLoginRequest request) {

    User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
        .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new BusinessException(ErrorCode.LOGIN_FAILED);
    }

    return UserResponse.from(user);
  }

  public UserResponse update(
      UUID userId,
      UUID requestUserId,
      UserUpdateRequest request
  ) {

    if (!userId.equals(requestUserId)) {
      throw new BusinessException(ErrorCode.USER_UPDATE_ACCESS_DENIED);
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    user.updateNickname(request.nickname());

    User updatedUser = userRepository.save(user);

    return UserResponse.from(updatedUser);
  }

  public void delete(UUID userId, UUID requestUserId) {

    if (!userId.equals(requestUserId)) {
      throw new BusinessException(ErrorCode.USER_DELETE_ACCESS_DENIED);
    }

    User user = userRepository.findByIdAndDeletedAtIsNull(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    user.delete();

    userRepository.save(user);
  }
}