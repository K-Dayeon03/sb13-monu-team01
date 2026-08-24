package com.project.monu.domain.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank
    @Size(max = 10)
    String nickname
) {
}