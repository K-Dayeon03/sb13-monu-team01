package com.project.monu.domain.useractivity.dto;

import java.time.Instant;
import java.util.UUID;

public record UserActivityCommentLikeResponse(
        UUID id,
        UUID likedBy,
        Instant createdAt,
        UUID commentId,
        UUID articleId,
        String articleTitle,
        UUID commentUserId,
        String commentUserNickname,
        String commentContent,
        long commentLikeCount,
        Instant commentCreatedAt
) {
}