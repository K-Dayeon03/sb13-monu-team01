package com.project.monu.domain.users.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(max = 10)
    String nickname,

    @NotBlank
    String password
) {
}