package com.project.monu.domain.users.service;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;

public class UserService {

  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public UserResponse create(UserCreateRequest request) {
    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(request.password())
        .build();

    User savedUser = userRepository.save(user);

    return new UserResponse(
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getNickname(),
        savedUser.getCreatedAt()
    );
  }
}