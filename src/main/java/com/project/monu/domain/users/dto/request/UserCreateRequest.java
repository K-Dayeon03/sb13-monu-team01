package com.project.monu.domain.users.dto.request;

public record UserCreateRequest(
    String email,
    String nickname,
    String password
) {
}