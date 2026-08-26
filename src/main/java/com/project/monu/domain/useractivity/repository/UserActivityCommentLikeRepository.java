package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
import java.util.List;
import java.util.UUID;

public interface UserActivityCommentLikeRepository {

    List<UserActivityCommentLikeResponse> findAllByUserId(UUID userId);
}