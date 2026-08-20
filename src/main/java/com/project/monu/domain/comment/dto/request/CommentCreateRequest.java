package com.project.monu.domain.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CommentCreateRequest(

        @NotNull
        UUID articleId,

        @NotNull
        UUID userId,

        @NotBlank
        @Size(max = 500)
        String content

) {
}