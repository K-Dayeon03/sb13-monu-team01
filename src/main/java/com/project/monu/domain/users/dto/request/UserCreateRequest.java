package com.project.monu.domain.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(

    @Email
    String email,

    @NotBlank
    String nickname,

    String password
) {
}