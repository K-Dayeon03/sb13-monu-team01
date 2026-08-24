package com.project.monu.domain.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

    @NotBlank(message = "닉네임은 1자 이상 20자 이하로 작성해주세요")
    @Size(max = 20, message = "닉네임은 1자 이상 20자 이하로 작성해주세요")
    String nickname
) {
}