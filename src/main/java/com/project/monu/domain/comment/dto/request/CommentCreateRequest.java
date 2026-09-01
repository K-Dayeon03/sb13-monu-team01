package com.project.monu.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentCreateRequest(

        @NotNull(message = "기사 ID는 필수입니다.")
        UUID articleId,

        @NotNull(message = "사용자 ID는 필수입니다.")
        UUID userId,

        @NotBlank(message = "댓글 내용을 입력해주세요.")
        @Size(max = 500, message = "댓글은 500자 이하로 작성해주세요.")
        String content

) {
}
