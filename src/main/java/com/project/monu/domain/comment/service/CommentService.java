package com.project.monu.domain.comment.service;

import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.CommentLikeDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.global.dto.CursorPageResponse;

import java.time.Instant;
import java.util.UUID;

public interface CommentService {

    CommentDto create(CommentCreateRequest request);

    CursorPageResponse<CommentDto> getComments(
            UUID articleId,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit,
            UUID requestUserId
    );

    CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request);

    void delete(UUID commentId, UUID requestUserId);

    void hardDelete(UUID commentId);

    CommentLikeDto like(UUID commentId, UUID requestUserId);

    void unlike(UUID commentId, UUID requestUserId);
}