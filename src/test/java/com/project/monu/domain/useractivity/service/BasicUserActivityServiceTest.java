package com.project.monu.domain.useractivity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import com.project.monu.domain.useractivity.repository.UserActivityArticleViewRepository;
import com.project.monu.domain.useractivity.repository.UserActivityCommentLikeRepository;
import com.project.monu.domain.useractivity.repository.UserActivityCommentRepository;
import com.project.monu.domain.useractivity.repository.UserActivitySubscriptionRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BasicUserActivityServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserActivitySubscriptionRepository subscriptionRepository =
            mock(UserActivitySubscriptionRepository.class);
    private final UserActivityCommentRepository commentRepository =
            mock(UserActivityCommentRepository.class);
    private final UserActivityCommentLikeRepository commentLikeRepository =
            mock(UserActivityCommentLikeRepository.class);
    private final UserActivityArticleViewRepository articleViewRepository =
            mock(UserActivityArticleViewRepository.class);


    private final UserActivityService userActivityService =
            new BasicUserActivityService(
                    userRepository,
                    subscriptionRepository,
                    commentRepository,
                    commentLikeRepository,
                    articleViewRepository
            );

    @Test
    void 존재하는_사용자의_활동_내역을_조회할_수_있다() {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .email("user@email.com")
                .nickname("사용자")
                .password("encoded-password")
                .build();

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        when(commentRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        UserActivityResponse response = userActivityService.getUserActivity(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@email.com");
        assertThat(response.nickname()).isEqualTo("사용자");
        assertThat(response.subscriptions()).isEmpty();
        assertThat(response.comments()).isEmpty();
        assertThat(response.commentLikes()).isEmpty();
        assertThat(response.articleViews()).isEmpty();

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verify(subscriptionRepository).findAllByUserId(userId);
        verify(commentRepository).findAllByUserId(userId);
    }

    @Test
    void 구독한_관심사_목록을_활동_내역에_포함한다() {
        UUID userId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        Instant subscribedAt = Instant.parse("2026-08-26T00:00:00Z");

        User user = User.builder()
                .email("user@email.com")
                .nickname("사용자")
                .password("encoded-password")
                .build();

        SubscriptionDto subscription = new SubscriptionDto(
                subscriptionId,
                interestId,
                "경제",
                List.of("금리", "환율"),
                10L,
                subscribedAt
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findAllByUserId(userId))
                .thenReturn(List.of(subscription));

        when(commentRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        UserActivityResponse response = userActivityService.getUserActivity(userId);

        assertThat(response.subscriptions()).hasSize(1);
        assertThat(response.subscriptions().get(0).interestId()).isEqualTo(interestId);
        assertThat(response.subscriptions().get(0).interestName()).isEqualTo("경제");
        assertThat(response.subscriptions().get(0).interestKeywords())
                .containsExactly("금리", "환율");

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verify(subscriptionRepository).findAllByUserId(userId);
        verify(commentRepository).findAllByUserId(userId);
    }

    @Test
    void 작성한_댓글_목록을_활동_내역에_포함한다() {
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Instant commentedAt = Instant.parse("2026-08-26T01:00:00Z");

        User user = User.builder()
                .email("user@email.com")
                .nickname("사용자")
                .password("encoded-password")
                .build();

        CommentDto comment = new CommentDto(
                commentId,
                articleId,
                userId,
                "사용자",
                "댓글 내용",
                3L,
                false,
                commentedAt
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        when(commentRepository.findAllByUserId(userId))
                .thenReturn(List.of(comment));

        UserActivityResponse response = userActivityService.getUserActivity(userId);

        assertThat(response.comments()).hasSize(1);
        assertThat(response.comments().get(0).id()).isEqualTo(commentId);
        assertThat(response.comments().get(0).articleId()).isEqualTo(articleId);
        assertThat(response.comments().get(0).content()).isEqualTo("댓글 내용");
        assertThat(response.comments().get(0).likeCount()).isEqualTo(3L);

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verify(subscriptionRepository).findAllByUserId(userId);
        verify(commentRepository).findAllByUserId(userId);
    }

    @Test
    void 존재하지_않는_사용자의_활동_내역은_조회할_수_없다() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userActivityService.getUserActivity(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }

    @Test
    void 좋아요한_댓글_목록을_활동_내역에_포함한다() {
        UUID userId = UUID.randomUUID();
        UUID commentLikeId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID commentUserId = UUID.randomUUID();
        Instant likedAt = Instant.parse("2026-08-26T02:00:00Z");
        Instant commentedAt = Instant.parse("2026-08-26T01:00:00Z");

        User user = User.builder()
                .email("user@email.com")
                .nickname("사용자")
                .password("encoded-password")
                .build();

        UserActivityCommentLikeResponse commentLike = new UserActivityCommentLikeResponse(
                commentLikeId,
                userId,
                likedAt,
                commentId,
                articleId,
                "기사 제목",
                commentUserId,
                "댓글작성자",
                "좋아요한 댓글",
                5L,
                commentedAt
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        when(commentRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        when(commentLikeRepository.findAllByUserId(userId))
                .thenReturn(List.of(commentLike));

        UserActivityResponse response = userActivityService.getUserActivity(userId);

        assertThat(response.commentLikes()).hasSize(1);
        assertThat(response.commentLikes().get(0).id()).isEqualTo(commentLikeId);
        assertThat(response.commentLikes().get(0).commentId()).isEqualTo(commentId);
        assertThat(response.commentLikes().get(0).articleTitle()).isEqualTo("기사 제목");
        assertThat(response.commentLikes().get(0).commentContent()).isEqualTo("좋아요한 댓글");
        assertThat(response.commentLikes().get(0).commentLikeCount()).isEqualTo(5L);

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verify(subscriptionRepository).findAllByUserId(userId);
        verify(commentRepository).findAllByUserId(userId);
        verify(commentLikeRepository).findAllByUserId(userId);
    }

    @Test
    void 조회한_기사_목록을_활동_내역에_포함한다() {
        UUID userId = UUID.randomUUID();
        UUID articleViewId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Instant viewedAt = Instant.parse("2026-08-26T03:00:00Z");
        Instant publishedAt = Instant.parse("2026-08-26T00:00:00Z");

        User user = User.builder()
                .email("user@email.com")
                .nickname("사용자")
                .password("encoded-password")
                .build();

        ArticleViewDto articleView = new ArticleViewDto(
                articleViewId,
                userId,
                viewedAt,
                articleId,
                "NAVER",
                "https://news.example.com/articles/1",
                "기사 제목",
                publishedAt,
                "기사 요약",
                4L,
                20L
        );

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(subscriptionRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        when(commentRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        when(commentLikeRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        when(articleViewRepository.findAllByUserId(userId))
                .thenReturn(List.of(articleView));

        UserActivityResponse response = userActivityService.getUserActivity(userId);

        assertThat(response.articleViews()).hasSize(1);
        assertThat(response.articleViews().get(0).id()).isEqualTo(articleViewId);
        assertThat(response.articleViews().get(0).articleId()).isEqualTo(articleId);
        assertThat(response.articleViews().get(0).source()).isEqualTo("NAVER");
        assertThat(response.articleViews().get(0).articleTitle()).isEqualTo("기사 제목");
        assertThat(response.articleViews().get(0).articleCommentCount()).isEqualTo(4L);
        assertThat(response.articleViews().get(0).articleViewCount()).isEqualTo(20L);

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verify(subscriptionRepository).findAllByUserId(userId);
        verify(commentRepository).findAllByUserId(userId);
        verify(commentLikeRepository).findAllByUserId(userId);
        verify(articleViewRepository).findAllByUserId(userId);
    }

}