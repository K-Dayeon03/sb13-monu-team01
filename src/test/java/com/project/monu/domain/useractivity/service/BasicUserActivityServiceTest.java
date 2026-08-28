package com.project.monu.domain.useractivity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.monu.domain.useractivity.document.UserActivityDocument;
import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import com.project.monu.domain.useractivity.repository.UserActivityArticleViewRepository;
import com.project.monu.domain.useractivity.repository.UserActivityCommentLikeRepository;
import com.project.monu.domain.useractivity.repository.UserActivityCommentRepository;
import com.project.monu.domain.useractivity.repository.UserActivityMongoRepository;
import com.project.monu.domain.useractivity.repository.UserActivitySubscriptionRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class BasicUserActivityServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserActivityMongoRepository mongoRepository =
            mock(UserActivityMongoRepository.class);
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
                    mongoRepository,
                    subscriptionRepository,
                    commentRepository,
                    commentLikeRepository,
                    articleViewRepository
            );

    @Test
    void 사용자_활동을_JPA로_조회하고_MongoDB_문서로_저장한다() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-28T00:00:00Z");
        User user = createUser("user@email.com", "사용자", createdAt);

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(commentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(commentLikeRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(articleViewRepository.findAllByUserId(userId)).thenReturn(List.of());

        UserActivityResponse response = userActivityService.getUserActivity(userId);

        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("user@email.com");
        assertThat(response.nickname()).isEqualTo("사용자");
        assertThat(response.createdAt()).isEqualTo(createdAt);

        ArgumentCaptor<UserActivityDocument> documentCaptor =
                ArgumentCaptor.forClass(UserActivityDocument.class);

        verify(mongoRepository).save(documentCaptor.capture());

        UserActivityResponse savedResponse = documentCaptor.getValue().toResponse();

        assertThat(savedResponse.id()).isEqualTo(userId);
        assertThat(savedResponse.email()).isEqualTo("user@email.com");
        assertThat(savedResponse.nickname()).isEqualTo("사용자");
        assertThat(savedResponse.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void 사용자_활동_조회는_MongoDB_문서_조회에_의존하지_않는다() {
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-28T00:00:00Z");
        User user = createUser("latest@email.com", "최신사용자", createdAt);

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(subscriptionRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(commentRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(commentLikeRepository.findAllByUserId(userId)).thenReturn(List.of());
        when(articleViewRepository.findAllByUserId(userId)).thenReturn(List.of());

        UserActivityResponse response = userActivityService.getUserActivity(userId);

        assertThat(response.email()).isEqualTo("latest@email.com");
        assertThat(response.nickname()).isEqualTo("최신사용자");

        verify(mongoRepository, never()).findById(userId);
        verify(mongoRepository).save(any(UserActivityDocument.class));
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
        verify(mongoRepository, never()).save(any(UserActivityDocument.class));
    }

    private User createUser(String email, String nickname, Instant createdAt) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password("encoded-password")
                .build();

        ReflectionTestUtils.setField(user, "createdAt", createdAt);

        return user;
    }
}