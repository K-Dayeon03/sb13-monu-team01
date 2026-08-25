package com.project.monu.domain.article.service;

import com.project.monu.domain.article.dto.response.ArticleDto;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.ArticleView;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.comment.repository.CommentLikeRepository;
import com.project.monu.domain.comment.repository.CommentRepository;
import com.project.monu.domain.notification.entity.NotificationResourceType;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.dto.CursorPageResponse;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
    private ArticleSourceRepository articleSourceRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private NotificationRepository notificationRepository;

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

    @Test
    void 기사를_물리_삭제하면_연관_데이터를_먼저_삭제한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        Article article = mock(Article.class);

        when(articleRepository.findById(articleId))
                .thenReturn(Optional.of(article));
        when(commentRepository.findIdsByArticleId(articleId))
                .thenReturn(List.of(commentId));

        // when
        articleService.hardDelete(articleId);

        // then
        InOrder inOrder = inOrder(
                commentLikeRepository,
                notificationRepository,
                commentRepository,
                articleInterestRepository,
                articleViewRepository,
                articleRepository
        );

        inOrder.verify(articleRepository)
                .findById(articleId);
        inOrder.verify(commentRepository)
                .findIdsByArticleId(articleId);
        inOrder.verify(commentLikeRepository)
                .deleteAllByComment_IdIn(List.of(commentId));
        inOrder.verify(notificationRepository)
                .deleteAllByResourceTypeAndResourceIdIn(
                        NotificationResourceType.COMMENT,
                        List.of(commentId)
                );
        inOrder.verify(commentRepository)
                .deleteAllByArticle_Id(articleId);
        inOrder.verify(articleInterestRepository)
                .deleteAllByArticle_Id(articleId);
        inOrder.verify(articleViewRepository)
                .deleteAllByArticle_Id(articleId);
        inOrder.verify(articleRepository)
                .delete(article);
    }

    @Test
    void 댓글이_없는_기사도_물리_삭제한다() {
        // given
        UUID articleId = UUID.randomUUID();
        Article article = mock(Article.class);

        when(articleRepository.findById(articleId))
                .thenReturn(Optional.of(article));
        when(commentRepository.findIdsByArticleId(articleId))
                .thenReturn(List.of());

        // when
        articleService.hardDelete(articleId);

        // then
        verify(commentLikeRepository, never()).deleteAllByComment_IdIn(any());
        verify(notificationRepository, never())
                .deleteAllByResourceTypeAndResourceIdIn(any(), any());
        verify(commentRepository).deleteAllByArticle_Id(articleId);
        verify(articleInterestRepository).deleteAllByArticle_Id(articleId);
        verify(articleViewRepository).deleteAllByArticle_Id(articleId);
        verify(articleRepository).delete(article);
    }

    @Test
    void 존재하지_않는_기사를_물리_삭제하면_예외가_발생한다() {
        // given
        UUID articleId = UUID.randomUUID();

        when(articleRepository.findById(articleId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.hardDelete(articleId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(businessException.getErrorCode())
                            .isEqualTo(ErrorCode.ARTICLE_NOT_FOUND);
                });

        verifyNoInteractions(commentLikeRepository);
        verifyNoInteractions(notificationRepository);
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(articleInterestRepository);
        verifyNoInteractions(articleViewRepository);
    }

    @Test
    void 활성화된_기사_출처_이름_목록을_반환한다() {
        // given
        ArticleSource naver = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://naver.example.com")
                .build();

        ArticleSource hankyung = ArticleSource.builder()
                .name("HANKYUNG")
                .type(SourceType.RSS)
                .sourceUrl("https://hankyung.example.com/rss")
                .build();

        when(articleSourceRepository.findAllByEnabledTrue())
                .thenReturn(List.of(naver, hankyung));

        // when
        List<String> result = articleService.getSources();

        // then
        assertThat(result)
                .containsExactly("NAVER", "HANKYUNG");

        verify(articleSourceRepository).findAllByEnabledTrue();
    }

    @Test
    void 이미_조회한_기사를_단건_조회하면_조회수를_증가시키지_않는다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);

        ArticleSource source = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://naver.example.com")
                .build();

        Article article = Article.builder()
                .source(source)
                .sourceUrl("https://example.com/article/1")
                .title("테스트 기사")
                .publishDate(Instant.parse("2026-08-24T00:00:00Z"))
                .summary("테스트 요약")
                .build();

        ReflectionTestUtils.setField(article, "id", articleId);

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId))
                .thenReturn(Optional.of(article));

        when(articleViewRepository.existsByViewerIdAndArticleId(userId, articleId))
                .thenReturn(true);

        // when
        ArticleDto result = articleService.getArticle(articleId, userId);

        // then
        assertThat(result.id()).isEqualTo(articleId);
        assertThat(result.source()).isEqualTo("NAVER");
        assertThat(result.sourceUrl())
                .isEqualTo("https://example.com/article/1");
        assertThat(result.title()).isEqualTo("테스트 기사");
        assertThat(result.summary()).isEqualTo("테스트 요약");
        assertThat(result.viewCount()).isZero();
        assertThat(result.viewedByMe()).isTrue();

        verify(userRepository)
                .findByIdAndDeletedAtIsNull(userId);
        verify(articleRepository)
                .findByIdAndDeletedAtIsNull(articleId);
        verify(articleViewRepository)
                .existsByViewerIdAndArticleId(userId, articleId);
        verify(articleViewRepository, never()).save(any());
    }

    @Test
    void 처음_조회한_기사를_단건_조회하면_조회_이력을_저장하고_조회수를_증가시킨다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);

        ArticleSource source = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://naver.example.com")
                .build();

        Article article = Article.builder()
                .source(source)
                .sourceUrl("https://example.com/article/1")
                .title("테스트 기사")
                .publishDate(Instant.parse("2026-08-24T00:00:00Z"))
                .summary("테스트 요약")
                .build();

        ReflectionTestUtils.setField(article, "id", articleId);

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId))
                .thenReturn(Optional.of(article));

        when(articleViewRepository.existsByViewerIdAndArticleId(userId, articleId))
                .thenReturn(false);

        // when
        ArticleDto result = articleService.getArticle(articleId, userId);

        // then
        assertThat(result.id()).isEqualTo(articleId);
        assertThat(result.viewCount()).isEqualTo(1L);
        assertThat(result.viewedByMe()).isTrue();
        assertThat(article.getViewCount()).isEqualTo(1L);

        ArgumentCaptor<ArticleView> articleViewCaptor =
                ArgumentCaptor.forClass(ArticleView.class);

        verify(articleViewRepository).save(articleViewCaptor.capture());

        ArticleView savedArticleView = articleViewCaptor.getValue();
        assertThat(savedArticleView.getViewer()).isEqualTo(user);
        assertThat(savedArticleView.getArticle()).isEqualTo(article);
    }

    @Test
    void 존재하지_않는_기사를_단건_조회하면_예외가_발생한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user(userId)));

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                articleService.getArticle(articleId, userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(businessException.getErrorCode())
                            .isEqualTo(ErrorCode.ARTICLE_NOT_FOUND);
                });

        verify(articleRepository)
                .findByIdAndDeletedAtIsNull(articleId);

        verifyNoInteractions(articleViewRepository);
    }

    @Test
    void 존재하지_않는_사용자가_기사를_단건_조회하면_예외가_발생한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                articleService.getArticle(articleId, userId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException =
                            (BusinessException) exception;

                    assertThat(businessException.getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND);
                });

        verify(userRepository)
                .findByIdAndDeletedAtIsNull(userId);

        verifyNoInteractions(articleRepository);
        verifyNoInteractions(articleViewRepository);
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

    private User user(UUID id) {
        User user = User.builder()
                .email(id + "@test.com")
                .nickname("tester")
                .password("encoded-password")
                .build();

        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
