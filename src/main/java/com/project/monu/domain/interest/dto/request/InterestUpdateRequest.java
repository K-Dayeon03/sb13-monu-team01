package com.project.monu.domain.interest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InterestUpdateRequest(
        @NotEmpty(message = "키워드는 1개 이상 입력해주세요.")
        @Size(max = 10, message = "키워드는 10개 이하로 입력해주세요.")
        List<@NotBlank(message = "키워드는 빈 값일 수 없습니다.") String> keywords
) {
}
