package com.project.monu.domain.users.dto.request;

import jakarta.validation.constraints.Email;

public record UserCreateRequest(

    @Email
    String email,
    String nickname,
    String password
) {
}