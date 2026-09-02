package com.project.monu.domain.useractivity.dto;

import java.time.Instant;
import java.util.UUID;

public record UserActivityCommentResponse(
        UUID id,
        UUID articleId,
        String articleTitle,
        UUID userId,
        String userNickname,
        String content,
        long likeCount,
        boolean likedByMe,
        Instant createdAt
) {
}