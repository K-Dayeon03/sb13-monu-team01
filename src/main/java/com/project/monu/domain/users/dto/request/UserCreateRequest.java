package com.project.monu.domain.users.dto.request;

import com.project.monu.domain.users.validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordMatches
public record UserCreateRequest(

    @NotBlank
    @Email
    String email,

    @NotBlank
    @Size(max = 10)
    String nickname,

    @NotBlank
    @Size(min = 6, max = 20)
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    String password,

    String passwordConfirm
) {
}