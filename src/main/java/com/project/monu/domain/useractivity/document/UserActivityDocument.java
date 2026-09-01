package com.project.monu.domain.useractivity.document;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_activities")
public class UserActivityDocument {

    @Id
    private UUID userId;

    private String email;
    private String nickname;
    private Instant createdAt;
    private List<SubscriptionDto> subscriptions;
    private List<CommentDto> comments;
    private List<UserActivityCommentLikeResponse> commentLikes;
    private List<ArticleViewDto> articleViews;
    private Instant updatedAt;

    protected UserActivityDocument() {
    }

    public UserActivityDocument(
            UUID userId,
            String email,
            String nickname,
            Instant createdAt,
            List<SubscriptionDto> subscriptions,
            List<CommentDto> comments,
            List<UserActivityCommentLikeResponse> commentLikes,
            List<ArticleViewDto> articleViews,
            Instant updatedAt
    ) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.createdAt = createdAt;
        this.subscriptions = subscriptions;
        this.comments = comments;
        this.commentLikes = commentLikes;
        this.articleViews = articleViews;
        this.updatedAt = updatedAt;
    }

    public static UserActivityDocument from(UserActivityResponse response, Instant updatedAt) {
        return new UserActivityDocument(
                response.id(),
                response.email(),
                response.nickname(),
                response.createdAt(),
                response.subscriptions(),
                response.comments(),
                response.commentLikes(),
                response.articleViews(),
                updatedAt
        );
    }

    public UserActivityResponse toResponse() {
        return new UserActivityResponse(
                userId,
                email,
                nickname,
                createdAt,
                subscriptions,
                comments,
                commentLikes,
                articleViews
        );
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}