package com.project.monu.domain.article.service;

import com.project.monu.domain.article.dto.ArticleDto;
import com.project.monu.domain.article.dto.CursorPageResponse;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleViewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleViewRepository articleViewRepository;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void 사용자_조회_이력이_있으면_viewedByMe가_true다() {
        // given
        // 현재 로그인 사용자와 기사 ID를 준비합니다.
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        // ArticleService는 Article 엔티티를 ArticleDto로 변환하므로,
        // 변환에 필요한 Article getter 값을 mock으로 준비합니다.
        Article article = article(articleId, "NAVER", 10L, 100L);

        // 목록 조회 조건입니다.
        // 이 테스트에서는 정렬/필터보다 viewedByMe 계산이 목적이라 기본 날짜 정렬 조건만 둡니다.
        ArticleSearchCondition condition = condition(10, ArticleSortType.PUBLISH_DATE);

        // Repository가 기사 1개를 반환한다고 가정합니다.
        when(articleRepository.searchByCursor(condition)).thenReturn(List.of(article));
        when(articleRepository.countByCondition(condition)).thenReturn(1L);

        // 현재 사용자가 해당 기사를 이미 조회한 상태라고 가정합니다.
        // ArticleService는 이 Set에 articleId가 포함되어 있으면 viewedByMe=true로 응답합니다.
        when(articleViewRepository.findViewedArticleIds(userId, List.of(articleId)))
                .thenReturn(Set.of(articleId));

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, userId);

        // then
        // 응답 DTO의 viewedByMe가 true인지 확인합니다.
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).viewedByMe()).isTrue();

        // 조회된 데이터가 size를 초과하지 않았으므로 다음 페이지는 없습니다.
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void userId가_null이면_viewedByMe는_false이고_조회이력은_조회하지_않는다() {
        // given
        UUID articleId = UUID.randomUUID();
        Article article = article(articleId, "NAVER", 10L, 100L);
        ArticleSearchCondition condition = condition(10, ArticleSortType.PUBLISH_DATE);

        when(articleRepository.searchByCursor(condition)).thenReturn(List.of(article));
        when(articleRepository.countByCondition(condition)).thenReturn(1L);

        // when
        // 인증 기능이 아직 붙지 않았거나 비로그인 상태라면 userId가 null일 수 있습니다.
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, null);

        // then
        // userId가 없으면 조회 이력을 확인할 수 없으므로 viewedByMe는 false입니다.
        assertThat(response.content().get(0).viewedByMe()).isFalse();

        // userId가 null이면 ArticleViewRepository를 호출하지 않아야 합니다.
        // 불필요한 쿼리를 막고, null 파라미터로 인한 오류도 방지합니다.
        verify(articleViewRepository, never()).findViewedArticleIds(any(), any());
    }

    @Test
    void size보다_많이_조회되면_hasNext가_true이고_COMMENT_COUNT_커서를_생성한다() {
        // given
        // Repository는 다음 페이지 존재 여부를 판단하기 위해 size + 1개를 조회합니다.
        // size=2일 때 3개가 반환되면 hasNext=true가 되어야 합니다.
        Article first = article(UUID.randomUUID(), "NAVER", 30L, 300L);
        Article second = article(UUID.randomUUID(), "NAVER", 20L, 200L);

        // extra는 hasNext 판단용으로만 사용되고 응답 변환 전에 잘립니다.
        // 따라서 getter stubbing이 필요 없고, 불필요한 stubbing 오류를 피하기 위해 단순 mock으로 둡니다.
        Article extra = mock(Article.class);

        ArticleSearchCondition condition = condition(2, ArticleSortType.COMMENT_COUNT);

        when(articleRepository.searchByCursor(condition)).thenReturn(List.of(first, second, extra));
        when(articleRepository.countByCondition(condition)).thenReturn(3L);

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, null);

        // then
        // 응답에는 요청한 size만큼만 담고, 추가 1개는 hasNext 판단에만 사용합니다.
        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isTrue();

        // 댓글 수 정렬에서는 다음 페이지 커서를 "댓글수_기사ID" 형태로 만듭니다.
        // 다음 요청에서 이 커서를 기준으로 댓글 수가 더 낮은 기사부터 이어서 조회합니다.
        assertThat(response.nextCursor()).isEqualTo(second.getCommentCount() + "_" + second.getId());
    }

    @Test
    void size보다_많이_조회되면_VIEW_COUNT_커서를_생성한다() {
        // given
        // 조회수 정렬은 "조회수_기사ID" 형태의 커서를 사용합니다.
        // 댓글 수와 같은 동점 처리 방식이므로 별도 분기로 검증합니다.
        Article first = article(UUID.randomUUID(), "NAVER", 30L, 300L);
        Article second = article(UUID.randomUUID(), "NAVER", 20L, 200L);
        Article extra = mock(Article.class);

        ArticleSearchCondition condition = condition(2, ArticleSortType.VIEW_COUNT);

        when(articleRepository.searchByCursor(condition)).thenReturn(List.of(first, second, extra));
        when(articleRepository.countByCondition(condition)).thenReturn(3L);

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, null);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isEqualTo(second.getViewCount() + "_" + second.getId());
    }

    @Test
    void size가_0이면_기본_size_10으로_조회한다() {
        // given
        // 클라이언트가 size를 0 이하로 보내면 서비스에서 기본값 10으로 보정합니다.
        // Repository에 전달되는 조건이 보정되었는지 ArgumentCaptor로 확인합니다.
        ArticleSearchCondition condition = condition(0, ArticleSortType.PUBLISH_DATE);

        when(articleRepository.searchByCursor(any(ArticleSearchCondition.class))).thenReturn(List.of());
        when(articleRepository.countByCondition(any(ArticleSearchCondition.class))).thenReturn(0L);

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, null);

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
        // 한 번에 너무 많은 기사를 요청하면 DB 부하가 커질 수 있으므로 최대 100개로 제한합니다.
        ArticleSearchCondition condition = condition(1000, ArticleSortType.PUBLISH_DATE);

        when(articleRepository.searchByCursor(any(ArticleSearchCondition.class))).thenReturn(List.of());
        when(articleRepository.countByCondition(any(ArticleSearchCondition.class))).thenReturn(0L);

        // when
        CursorPageResponse<ArticleDto> response = articleService.getArticles(condition, null);

        // then
        ArgumentCaptor<ArticleSearchCondition> conditionCaptor =
                ArgumentCaptor.forClass(ArticleSearchCondition.class);

        verify(articleRepository).searchByCursor(conditionCaptor.capture());
        assertThat(conditionCaptor.getValue().size()).isEqualTo(100);
        assertThat(response.size()).isEqualTo(100);
    }

    /**
     * 테스트에서 사용할 목록 조회 조건을 만듭니다.
     * 필요한 값만 테스트별로 다르게 넣고, 검색/필터 조건은 null로 둡니다.
     */
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

    /**
     * Article 엔티티를 직접 생성하지 않고 mock으로 만드는 이유:
     * 현재 Article은 JPA 엔티티라 생성자/setter가 제한되어 있을 수 있습니다.
     * Service 테스트에서는 DB 저장이 목적이 아니라 DTO 변환 로직 검증이 목적이므로,
     * 필요한 getter 값만 mock으로 지정합니다.
     */
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
