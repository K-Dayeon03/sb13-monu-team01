package com.project.monu.domain.comment.repository;

import com.project.monu.domain.comment.entity.Comment;

public record CommentQueryResult(
        Comment comment,
        long likeCount,
        boolean likedByMe
) {
}