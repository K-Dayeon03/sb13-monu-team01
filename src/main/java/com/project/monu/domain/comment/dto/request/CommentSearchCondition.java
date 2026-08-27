package com.project.monu.domain.comment.dto.request;

import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.UUID;

public record CommentSearchCondition(
        UUID articleId,
        CommentSortType sortType,
        Sort.Direction direction,
        String cursor,
        Instant after,
        int limit,
        UUID requestUserId
) {
}