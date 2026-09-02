package com.project.monu.domain.useractivity.dto;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserActivityResponse(
        UUID id,
        String email,
        String nickname,
        Instant createdAt,
        List<SubscriptionDto> subscriptions,
        List<UserActivityCommentResponse> comments,
        List<UserActivityCommentLikeResponse> commentLikes,
        List<ArticleViewDto> articleViews
) {
}