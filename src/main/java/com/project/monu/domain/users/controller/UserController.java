package com.project.monu.domain.users.controller;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.request.UserLoginRequest;
import com.project.monu.domain.users.dto.request.UserUpdateRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import com.project.monu.domain.users.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final String USER_ID_HEADER = "Monew-Request-User-ID";

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse create(
      @Valid @RequestBody UserCreateRequest request
  ) {
    return userService.create(request);
  }

  @PostMapping("/login")
  public UserResponse login(
      @Valid @RequestBody UserLoginRequest request
  ) {
    return userService.login(request);
  }

  @PatchMapping("/{userId}")
  public UserResponse update(
      @PathVariable UUID userId,
      @RequestHeader(value = USER_ID_HEADER, required = false) UUID requestUserId,
      @Valid @RequestBody UserUpdateRequest request
  ) {
    UUID requester = (requestUserId != null) ? requestUserId : userId;

    return userService.update(userId, requester, request);
  }
}