package com.project.monu.domain.useractivity.document;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserActivityDocumentTest {

    @Test
    void 사용자_활동_응답을_MongoDB_문서로_변환한다() {
        UUID userId = UUID.randomUUID();
        UserActivityResponse response = createUserActivityResponse(userId);

        UserActivityDocument document = UserActivityDocument.from(response);

        assertThat(document.getUserId()).isEqualTo(userId);
        assertThat(document.getUpdatedAt()).isNotNull();
        assertThat(document.toResponse()).isEqualTo(response);
    }

    @Test
    void MongoDB_문서를_사용자_활동_응답으로_변환한다() {
        UUID userId = UUID.randomUUID();
        UserActivityResponse response = createUserActivityResponse(userId);

        UserActivityDocument document = new UserActivityDocument(
                response.id(),
                response.email(),
                response.nickname(),
                response.createdAt(),
                response.subscriptions(),
                response.comments(),
                response.commentLikes(),
                response.articleViews(),
                Instant.parse("2026-08-28T05:00:00Z")
        );

        UserActivityResponse result = document.toResponse();

        assertThat(result).isEqualTo(response);

        assertThat(result.subscriptions().get(0).interestName()).isEqualTo("경제");
        assertThat(result.subscriptions().get(0).interestKeywords())
                .containsExactly("금리", "환율");
        assertThat(result.comments().get(0).content()).isEqualTo("내가 작성한 댓글");
        assertThat(result.commentLikes().get(0).commentContent()).isEqualTo("좋아요한 댓글");
        assertThat(result.articleViews().get(0).articleTitle()).isEqualTo("조회한 기사 제목");
    }

    private UserActivityResponse createUserActivityResponse(UUID userId) {
        UUID interestId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID commentArticleId = UUID.randomUUID();
        UUID commentLikeId = UUID.randomUUID();
        UUID likedCommentId = UUID.randomUUID();
        UUID likedCommentArticleId = UUID.randomUUID();
        UUID commentUserId = UUID.randomUUID();
        UUID articleViewId = UUID.randomUUID();
        UUID viewedArticleId = UUID.randomUUID();

        SubscriptionDto subscription = new SubscriptionDto(
                subscriptionId,
                interestId,
                "경제",
                List.of("금리", "환율"),
                10L,
                Instant.parse("2026-08-28T01:00:00Z")
        );

        CommentDto comment = new CommentDto(
                commentId,
                commentArticleId,
                userId,
                "사용자",
                "내가 작성한 댓글",
                3L,
                false,
                Instant.parse("2026-08-28T02:00:00Z")
        );

        UserActivityCommentLikeResponse commentLike = new UserActivityCommentLikeResponse(
                commentLikeId,
                userId,
                Instant.parse("2026-08-28T03:00:00Z"),
                likedCommentId,
                likedCommentArticleId,
                "좋아요한 댓글의 기사",
                commentUserId,
                "댓글작성자",
                "좋아요한 댓글",
                5L,
                Instant.parse("2026-08-28T02:30:00Z")
        );

        ArticleViewDto articleView = new ArticleViewDto(
                articleViewId,
                userId,
                Instant.parse("2026-08-28T04:00:00Z"),
                viewedArticleId,
                "NAVER",
                "https://news.example.com/articles/1",
                "조회한 기사 제목",
                Instant.parse("2026-08-28T00:30:00Z"),
                "조회한 기사 요약",
                4L,
                20L
        );

        return new UserActivityResponse(
                userId,
                "user@email.com",
                "사용자",
                Instant.parse("2026-08-28T00:00:00Z"),
                List.of(subscription),
                List.of(comment),
                List.of(commentLike),
                List.of(articleView)
        );
    }
}