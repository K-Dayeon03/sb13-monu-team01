package com.project.monu.domain.users.dto.request;

public record UserLoginRequest(
    String email,
    String password
) {
}