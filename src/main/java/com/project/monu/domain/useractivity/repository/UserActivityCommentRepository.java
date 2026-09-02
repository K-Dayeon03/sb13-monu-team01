package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.useractivity.dto.UserActivityCommentResponse;
import java.util.List;
import java.util.UUID;

public interface UserActivityCommentRepository {

    List<UserActivityCommentResponse> findAllByUserId(UUID userId);
}