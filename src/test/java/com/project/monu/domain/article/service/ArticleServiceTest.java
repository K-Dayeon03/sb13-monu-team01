package com.project.monu.domain.article.service;

import com.project.monu.domain.article.dto.ArticleDto;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.dto.CursorPageResponse;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void 사용자_조회_이력이_있으면_viewedByMe가_true다() {
        // given
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        Article article = article(articleId, "NAVER", 10L, 100L);
        ArticleSearchCondition condition = condition(10, ArticleSortType.PUBLISH_DATE);

        when(userRepository.existsByIdAndDeletedAtIsNull(userId)).thenReturn(true);
        when(articleRepository.searchByCursor(condition)).thenReturn(List.of(article));
        when(articleRepository.countByCondition(condition)).thenReturn(1L);
        when(articleViewRepository.findViewedArticleIds(userId, List.of(articleId)))
                .thenReturn(Set.of(articleId));

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, userId);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).viewedByMe()).isTrue();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void 존재하지_않는_사용자면_예외가_발생한다() {
        // given
        UUID userId = UUID.randomUUID();
        ArticleSearchCondition condition = condition(10, ArticleSortType.PUBLISH_DATE);

        when(userRepository.existsByIdAndDeletedAtIsNull(userId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(condition, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(articleRepository, never()).searchByCursor(any());
        verify(articleViewRepository, never()).findViewedArticleIds(any(), any());
    }

    @Test
    void 사용자_ID가_null이면_예외가_발생한다() {
        // given
        ArticleSearchCondition condition = condition(10, ArticleSortType.PUBLISH_DATE);

        // when & then
        assertThatThrownBy(() -> articleService.getArticles(condition, null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository, never()).existsByIdAndDeletedAtIsNull(any());
        verify(articleRepository, never()).searchByCursor(any());
        verify(articleViewRepository, never()).findViewedArticleIds(any(), any());
    }

    @Test
    void size보다_많이_조회되면_hasNext가_true이고_COMMENT_COUNT_커서를_생성한다() {
        // given
        UUID userId = UUID.randomUUID();

        Article first = article(UUID.randomUUID(), "NAVER", 30L, 300L);
        Article second = article(UUID.randomUUID(), "NAVER", 20L, 200L);
        Article extra = mock(Article.class);

        ArticleSearchCondition condition = condition(2, ArticleSortType.COMMENT_COUNT);

        when(userRepository.existsByIdAndDeletedAtIsNull(userId)).thenReturn(true);
        when(articleRepository.searchByCursor(condition)).thenReturn(List.of(first, second, extra));
        when(articleRepository.countByCondition(condition)).thenReturn(3L);
        when(articleViewRepository.findViewedArticleIds(
                userId,
                List.of(first.getId(), second.getId())
        )).thenReturn(Set.of());

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, userId);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(second.getCommentCount() + "_" + second.getId());
    }

    @Test
    void size보다_많이_조회되면_VIEW_COUNT_커서를_생성한다() {
        // given
        UUID userId = UUID.randomUUID();

        Article first = article(UUID.randomUUID(), "NAVER", 30L, 300L);
        Article second = article(UUID.randomUUID(), "NAVER", 20L, 200L);
        Article extra = mock(Article.class);

        ArticleSearchCondition condition = condition(2, ArticleSortType.VIEW_COUNT);

        when(userRepository.existsByIdAndDeletedAtIsNull(userId)).thenReturn(true);
        when(articleRepository.searchByCursor(condition)).thenReturn(List.of(first, second, extra));
        when(articleRepository.countByCondition(condition)).thenReturn(3L);
        when(articleViewRepository.findViewedArticleIds(
                userId,
                List.of(first.getId(), second.getId())
        )).thenReturn(Set.of());

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, userId);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(second.getViewCount() + "_" + second.getId());
    }

    @Test
    void size가_0이면_기본_size_10으로_조회한다() {
        // given
        UUID userId = UUID.randomUUID();
        ArticleSearchCondition condition = condition(0, ArticleSortType.PUBLISH_DATE);

        when(userRepository.existsByIdAndDeletedAtIsNull(userId)).thenReturn(true);
        when(articleRepository.searchByCursor(any(ArticleSearchCondition.class))).thenReturn(List.of());
        when(articleRepository.countByCondition(any(ArticleSearchCondition.class))).thenReturn(0L);

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, userId);

        // then
        ArgumentCaptor<ArticleSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(ArticleSearchCondition.class);

        verify(articleRepository).searchByCursor(conditionCaptor.capture());
        assertThat(conditionCaptor.getValue().size()).isEqualTo(10);
        assertThat(response.size()).isEqualTo(10);
    }

    @Test
    void size가_100보다_크면_최대_size_100으로_조회한다() {
        // given
        UUID userId = UUID.randomUUID();
        ArticleSearchCondition condition = condition(1000, ArticleSortType.PUBLISH_DATE);

        when(userRepository.existsByIdAndDeletedAtIsNull(userId)).thenReturn(true);
        when(articleRepository.searchByCursor(any(ArticleSearchCondition.class))).thenReturn(List.of());
        when(articleRepository.countByCondition(any(ArticleSearchCondition.class))).thenReturn(0L);

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, userId);

        // then
        ArgumentCaptor<ArticleSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(ArticleSearchCondition.class);

        verify(articleRepository).searchByCursor(conditionCaptor.capture());
        assertThat(conditionCaptor.getValue().size()).isEqualTo(100);
        assertThat(response.size()).isEqualTo(100);
    }

    @Test
    void 존재하는_기사를_논리_삭제한다() {
        // given
        UUID articleId = UUID.randomUUID();
        Article article = mock(Article.class);

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId))
                .thenReturn(Optional.of(article));

        // when
        articleService.softDelete(articleId);

        // then
        verify(articleRepository).findByIdAndDeletedAtIsNull(articleId);
        verify(article).softDelete();
    }

    @Test
    void 존재하지_않는_기사를_논리_삭제하면_예외가_발생한다() {
        // given
        UUID articleId = UUID.randomUUID();

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.softDelete(articleId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(businessException.getErrorCode())
                            .isEqualTo(ErrorCode.ARTICLE_NOT_FOUND);
                });
    }

    private ArticleSearchCondition condition(int size, ArticleSortType sortType) {
        return new ArticleSearchCondition(
                null,
                null,
                null,
                null,
                null,
                sortType,
                null,
                null,
                size
        );
    }

    private Article article(UUID id, String sourceName, Long commentCount, Long viewCount) {
        Article article = mock(Article.class);
        ArticleSource source = mock(ArticleSource.class);

        when(article.getId()).thenReturn(id);
        when(source.getName()).thenReturn(sourceName);
        when(article.getSource()).thenReturn(source);
        when(article.getSourceUrl()).thenReturn("https://example.com/" + id);
        when(article.getTitle()).thenReturn("title");
        when(article.getPublishDate()).thenReturn(Instant.parse("2026-08-18T00:00:00Z"));
        when(article.getSummary()).thenReturn("summary");
        when(article.getCommentCount()).thenReturn(commentCount);
        when(article.getViewCount()).thenReturn(viewCount);

        return article;
    }
}
