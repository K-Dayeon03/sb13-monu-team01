package com.project.monu.domain.comment.service;

import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.CommentLikeDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;

import java.util.UUID;

public interface CommentService {

    CommentDto create(CommentCreateRequest request);

    CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request);

    void delete(UUID commentId, UUID requestUserId);

    void hardDelete(UUID commentId);

    CommentLikeDto like(UUID commentId, UUID requestUserId);

    void unlike(UUID commentId, UUID requestUserId);
}