package com.project.monu.domain.useractivity.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
import com.project.monu.domain.useractivity.dto.UserActivityCommentResponse;
import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import com.project.monu.domain.useractivity.service.UserActivityService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserActivityController.class)
class UserActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserActivityService userActivityService;

    @Test
    @DisplayName("사용자 활동 내역을 조회한다")
    void getUserActivity() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID commentArticleId = UUID.randomUUID();
        UUID commentLikeId = UUID.randomUUID();
        UUID likedCommentId = UUID.randomUUID();
        UUID likedCommentArticleId = UUID.randomUUID();
        UUID commentUserId = UUID.randomUUID();
        UUID articleViewId = UUID.randomUUID();
        UUID viewedArticleId = UUID.randomUUID();

        Instant userCreatedAt = Instant.parse("2026-08-26T00:00:00Z");
        Instant subscribedAt = Instant.parse("2026-08-26T01:00:00Z");
        Instant commentedAt = Instant.parse("2026-08-26T02:00:00Z");
        Instant likedAt = Instant.parse("2026-08-26T03:00:00Z");
        Instant likedCommentCreatedAt = Instant.parse("2026-08-26T02:30:00Z");
        Instant viewedAt = Instant.parse("2026-08-26T04:00:00Z");
        Instant articlePublishedAt = Instant.parse("2026-08-26T00:30:00Z");

        SubscriptionDto subscription = new SubscriptionDto(
                subscriptionId,
                interestId,
                "경제",
                List.of("금리", "환율"),
                10L,
                subscribedAt
        );

        UserActivityCommentResponse comment = new UserActivityCommentResponse(
                commentId,
                commentArticleId,
                "댓글을 작성한 기사",
                userId,
                "사용자",
                "내가 작성한 댓글",
                3L,
                false,
                commentedAt
        );

        UserActivityCommentLikeResponse commentLike = new UserActivityCommentLikeResponse(
                commentLikeId,
                userId,
                likedAt,
                likedCommentId,
                likedCommentArticleId,
                "좋아요한 댓글의 기사",
                commentUserId,
                "댓글작성자",
                "좋아요한 댓글",
                5L,
                likedCommentCreatedAt
        );

        ArticleViewDto articleView = new ArticleViewDto(
                articleViewId,
                userId,
                viewedAt,
                viewedArticleId,
                "NAVER",
                "https://news.example.com/articles/1",
                "조회한 기사 제목",
                articlePublishedAt,
                "조회한 기사 요약",
                4L,
                20L
        );

        UserActivityResponse response = new UserActivityResponse(
                userId,
                "user@email.com",
                "사용자",
                userCreatedAt,
                List.of(subscription),
                List.of(comment),
                List.of(commentLike),
                List.of(articleView)
        );

        given(userActivityService.getUserActivity(userId)).willReturn(response);

        mockMvc.perform(get("/api/user-activities/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@email.com"))
                .andExpect(jsonPath("$.nickname").value("사용자"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-26T00:00:00Z"))

                .andExpect(jsonPath("$.subscriptions[0].id").value(subscriptionId.toString()))
                .andExpect(jsonPath("$.subscriptions[0].interestId").value(interestId.toString()))
                .andExpect(jsonPath("$.subscriptions[0].interestName").value("경제"))
                .andExpect(jsonPath("$.subscriptions[0].interestKeywords[0]").value("금리"))
                .andExpect(jsonPath("$.subscriptions[0].interestKeywords[1]").value("환율"))
                .andExpect(jsonPath("$.subscriptions[0].interestSubscriberCount").value(10))
                .andExpect(jsonPath("$.subscriptions[0].createdAt").value("2026-08-26T01:00:00Z"))

                .andExpect(jsonPath("$.comments[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$.comments[0].articleId").value(commentArticleId.toString()))
                .andExpect(jsonPath("$.comments[0].articleTitle").value("댓글을 작성한 기사"))
                .andExpect(jsonPath("$.comments[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.comments[0].userNickname").value("사용자"))
                .andExpect(jsonPath("$.comments[0].content").value("내가 작성한 댓글"))
                .andExpect(jsonPath("$.comments[0].likeCount").value(3))
                .andExpect(jsonPath("$.comments[0].likedByMe").value(false))
                .andExpect(jsonPath("$.comments[0].createdAt").value("2026-08-26T02:00:00Z"))

                .andExpect(jsonPath("$.commentLikes[0].id").value(commentLikeId.toString()))
                .andExpect(jsonPath("$.commentLikes[0].likedBy").value(userId.toString()))
                .andExpect(jsonPath("$.commentLikes[0].createdAt").value("2026-08-26T03:00:00Z"))
                .andExpect(jsonPath("$.commentLikes[0].commentId").value(likedCommentId.toString()))
                .andExpect(jsonPath("$.commentLikes[0].articleId").value(likedCommentArticleId.toString()))
                .andExpect(jsonPath("$.commentLikes[0].articleTitle").value("좋아요한 댓글의 기사"))
                .andExpect(jsonPath("$.commentLikes[0].commentUserId").value(commentUserId.toString()))
                .andExpect(jsonPath("$.commentLikes[0].commentUserNickname").value("댓글작성자"))
                .andExpect(jsonPath("$.commentLikes[0].commentContent").value("좋아요한 댓글"))
                .andExpect(jsonPath("$.commentLikes[0].commentLikeCount").value(5))
                .andExpect(jsonPath("$.commentLikes[0].commentCreatedAt").value("2026-08-26T02:30:00Z"))

                .andExpect(jsonPath("$.articleViews[0].id").value(articleViewId.toString()))
                .andExpect(jsonPath("$.articleViews[0].viewedBy").value(userId.toString()))
                .andExpect(jsonPath("$.articleViews[0].createdAt").value("2026-08-26T04:00:00Z"))
                .andExpect(jsonPath("$.articleViews[0].articleId").value(viewedArticleId.toString()))
                .andExpect(jsonPath("$.articleViews[0].source").value("NAVER"))
                .andExpect(jsonPath("$.articleViews[0].sourceUrl").value("https://news.example.com/articles/1"))
                .andExpect(jsonPath("$.articleViews[0].articleTitle").value("조회한 기사 제목"))
                .andExpect(jsonPath("$.articleViews[0].articlePublishedDate").value("2026-08-26T00:30:00Z"))
                .andExpect(jsonPath("$.articleViews[0].articleSummary").value("조회한 기사 요약"))
                .andExpect(jsonPath("$.articleViews[0].articleCommentCount").value(4))
                .andExpect(jsonPath("$.articleViews[0].articleViewCount").value(20));

        verify(userActivityService).getUserActivity(userId);
    }
}