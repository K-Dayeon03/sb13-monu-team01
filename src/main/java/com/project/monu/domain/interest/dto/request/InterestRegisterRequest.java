package com.project.monu.domain.interest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InterestRegisterRequest(
        @NotBlank(message = "관심사 이름을 입력해주세요.")
        @Size(min = 1, max = 50, message = "관심사 이름은 1자 이상 50자 이하로 작성해주세요.")
        String name,

        @NotEmpty(message = "키워드는 1개 이상 입력해주세요.")
        @Size(min = 1, max = 10, message = "키워드는 1개 이상 10개 이하로 입력해주세요.")
        List<@NotBlank(message = "키워드는 빈 값일 수 없습니다.") String> keywords
) {
}
