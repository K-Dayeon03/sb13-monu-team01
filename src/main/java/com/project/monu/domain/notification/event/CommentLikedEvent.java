package com.project.monu.domain.notification.event;

import java.util.UUID;

public record CommentLikedEvent(
        UUID commentAuthorId,
        UUID likedByUserId,
        String likedByUserNickname,
        UUID commentId
) {
}