package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.comment.dto.CommentDto;
import java.util.List;
import java.util.UUID;

public interface UserActivityCommentRepository {

    List<CommentDto> findAllByUserId(UUID userId);
}