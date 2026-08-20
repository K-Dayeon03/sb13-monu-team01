package com.project.monu.domain.comment.service;

import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.CommentLikeDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.repository.CommentLikeRepository;
import com.project.monu.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public CommentDto create(CommentCreateRequest request) {
        return null;
    }

    @Override
    public CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
        return null;
    }

    @Override
    public void delete(UUID commentId, UUID requestUserId) {
    }

    @Override
    public void hardDelete(UUID commentId) {
    }

    @Override
    public CommentLikeDto like(UUID commentId, UUID requestUserId) {
        return null;
    }

    @Override
    public void unlike(UUID commentId, UUID requestUserId) {
    }
}