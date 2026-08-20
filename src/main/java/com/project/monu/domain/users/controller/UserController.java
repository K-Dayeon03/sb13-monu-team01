package com.project.monu.domain.users.controller;

import com.project.monu.domain.users.dto.request.UserCreateRequest;
import com.project.monu.domain.users.dto.request.UserLoginRequest;
import com.project.monu.domain.users.dto.response.UserResponse;
import com.project.monu.domain.users.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

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
      @RequestBody UserLoginRequest request
  ) {
    return userService.login(request);
  }
}