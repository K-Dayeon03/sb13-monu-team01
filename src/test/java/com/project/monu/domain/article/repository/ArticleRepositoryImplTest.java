package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.entity.*;
import com.project.monu.domain.article.exception.InvalidArticleCursorException;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
class ArticleRepositoryImplTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private EntityManager em;

    @Test
    void keyword가_제목에_포함되면_조회된다() {
        // given
        ArticleSource source = source("NAVER");
        article(source, "AI 뉴스", "요약입니다", "2026-08-18T00:00:00Z", 1L, 10L);
        article(source, "경제 뉴스", "다른 요약", "2026-08-17T00:00:00Z", 1L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                "AI", null, null, null, null,
                ArticleSortType.PUBLISH_DATE, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("AI");
    }

    @Test
    void keyword가_요약에_포함되면_조회된다() {
        // given
        ArticleSource source = source("NAVER");
        article(source, "기술 뉴스", "AI 산업 동향 요약", "2026-08-18T00:00:00Z", 1L, 10L);
        article(source, "경제 뉴스", "다른 요약", "2026-08-17T00:00:00Z", 1L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                "산업", null, null, null, null,
                ArticleSortType.PUBLISH_DATE, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSummary()).contains("산업");
    }

    @Test
    void source로_필터링된다() {
        // given
        ArticleSource naver = source("NAVER");
        ArticleSource chosun = source("CHOSUN");

        article(naver, "네이버 기사", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        article(chosun, "조선 기사", "요약", "2026-08-17T00:00:00Z", 1L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, "NAVER", null, null,
                ArticleSortType.PUBLISH_DATE, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSource().getName()).isEqualTo("NAVER");
    }

    @Test
    void 발행일_범위로_필터링된다() {
        // given
        ArticleSource source = source("NAVER");
        Article oldArticle = article(source, "오래된 기사", "요약", "2026-08-10T00:00:00Z", 1L, 10L);
        Article targetArticle = article(source, "범위 안 기사", "요약", "2026-08-15T00:00:00Z", 1L, 10L);
        Article recentArticle = article(source, "최신 기사", "요약", "2026-08-20T00:00:00Z", 1L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null,
                Instant.parse("2026-08-14T00:00:00Z"),
                Instant.parse("2026-08-16T00:00:00Z"),
                ArticleSortType.PUBLISH_DATE, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(targetArticle.getId())
                .doesNotContain(oldArticle.getId(), recentArticle.getId());
    }

    @Test
    void 삭제된_기사는_조회되지_않는다() {
        // given
        ArticleSource source = source("NAVER");
        Article activeArticle = article(source, "정상 기사", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article deletedArticle = article(source, "삭제 기사", "요약", "2026-08-17T00:00:00Z", 1L, 10L);
        ReflectionTestUtils.setField(deletedArticle, "deletedAt", Instant.parse("2026-08-18T01:00:00Z"));
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.PUBLISH_DATE, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(activeArticle.getId())
                .doesNotContain(deletedArticle.getId());
    }

    @Test
    void 관심사_ID로_필터링된다() {
        // given
        ArticleSource source = source("NAVER");
        Interest interest = Interest.create("관심사");
        Interest otherInterest = Interest.create("다른관심사");
        em.persist(interest);
        em.persist(otherInterest);

        Article matchedArticle = article(source, "관심사 매칭 기사", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article otherArticle = article(source, "다른 관심사 기사", "요약", "2026-08-17T00:00:00Z", 1L, 10L);
        articleInterest(matchedArticle, interest);
        articleInterest(otherArticle, otherInterest);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, interest.getId(), null, null, null,
                ArticleSortType.PUBLISH_DATE, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(matchedArticle.getId());
    }

    @Test
    void 댓글수_기준으로_내림차순_정렬된다() {
        // given
        ArticleSource source = source("NAVER");

        Article low = article(source, "댓글 적은 기사", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article high = article(source, "댓글 많은 기사", "요약", "2026-08-17T00:00:00Z", 100L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.COMMENT_COUNT, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(high.getId(), low.getId());
    }

    @Test
    void 발행일이_같으면_ID_내림차순으로_정렬된다() {
        // given
        ArticleSource source = source("NAVER");
        Article first = article(source, "같은 발행일 기사1", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article second = article(source, "같은 발행일 기사2", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article third = article(source, "같은 발행일 기사3", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.PUBLISH_DATE, null, null, 10
        );

        List<UUID> expectedIds = List.of(first, second, third).stream()
                .map(Article::getId)
                .sorted(Comparator.comparing(UUID::toString).reversed())
                .toList();

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactlyElementsOf(expectedIds);
    }

    @Test
    void 발행일이_같은_경우_ID_커서로_다음_기사를_조회한다() {
        // given
        ArticleSource source = source("NAVER");
        Article first = article(source, "같은 발행일 기사1", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article second = article(source, "같은 발행일 기사2", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        flushAndClear();

        UUID cursorId = List.of(first.getId(), second.getId()).stream()
                .max(Comparator.comparing(UUID::toString))
                .orElseThrow();
        UUID expectedId = List.of(first.getId(), second.getId()).stream()
                .min(Comparator.comparing(UUID::toString))
                .orElseThrow();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.PUBLISH_DATE,
                Instant.parse("2026-08-18T00:00:00Z"),
                cursorId.toString(),
                10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(expectedId);
    }

    @Test
    void 발행일_커서_이후의_기사를_조회한다() {
        // given
        ArticleSource source = source("NAVER");
        Article latest = article(source, "최신 기사", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article middle = article(source, "중간 기사", "요약", "2026-08-17T00:00:00Z", 1L, 10L);
        Article oldest = article(source, "오래된 기사", "요약", "2026-08-16T00:00:00Z", 1L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.PUBLISH_DATE,
                latest.getPublishDate(),
                latest.getId().toString(),
                10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(middle.getId(), oldest.getId());
    }

    @Test
    void 댓글수_커서_이후의_기사를_조회한다() {
        // given
        ArticleSource source = source("NAVER");
        Article high = article(source, "댓글 많은 기사", "요약", "2026-08-18T00:00:00Z", 100L, 10L);
        Article middle = article(source, "댓글 중간 기사", "요약", "2026-08-17T00:00:00Z", 50L, 10L);
        Article low = article(source, "댓글 적은 기사", "요약", "2026-08-16T00:00:00Z", 10L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.COMMENT_COUNT,
                null,
                high.getCommentCount() + "_" + high.getId(),
                10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(middle.getId(), low.getId());
    }

    @Test
    void 댓글수가_같은_경우_ID_커서로_다음_기사를_조회한다() {
        // given
        ArticleSource source = source("NAVER");
        Article first = article(source, "댓글수 같은 기사1", "요약", "2026-08-18T00:00:00Z", 50L, 10L);
        Article second = article(source, "댓글수 같은 기사2", "요약", "2026-08-17T00:00:00Z", 50L, 10L);
        flushAndClear();

        UUID cursorId = List.of(first.getId(), second.getId()).stream()
                .max(Comparator.comparing(UUID::toString))
                .orElseThrow();
        UUID expectedId = List.of(first.getId(), second.getId()).stream()
                .min(Comparator.comparing(UUID::toString))
                .orElseThrow();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.COMMENT_COUNT,
                null,
                "50_" + cursorId,
                10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(expectedId);
    }

    @Test
    void 조회수_기준으로_내림차순_정렬된다() {
        // given
        ArticleSource source = source("NAVER");

        Article low = article(source, "조회수 적은 기사", "요약", "2026-08-18T00:00:00Z", 1L, 10L);
        Article high = article(source, "조회수 많은 기사", "요약", "2026-08-17T00:00:00Z", 1L, 100L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.VIEW_COUNT, null, null, 10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(high.getId(), low.getId());
    }

    @Test
    void 조회수_커서_이후의_기사를_조회한다() {
        // given
        ArticleSource source = source("NAVER");
        Article high = article(source, "조회수 많은 기사", "요약", "2026-08-18T00:00:00Z", 1L, 100L);
        Article middle = article(source, "조회수 중간 기사", "요약", "2026-08-17T00:00:00Z", 1L, 50L);
        Article low = article(source, "조회수 적은 기사", "요약", "2026-08-16T00:00:00Z", 1L, 10L);
        flushAndClear();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.VIEW_COUNT,
                null,
                high.getViewCount() + "_" + high.getId(),
                10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(middle.getId(), low.getId());
    }

    @Test
    void 조회수가_같은_경우_ID_커서로_다음_기사를_조회한다() {
        // given
        ArticleSource source = source("NAVER");
        Article first = article(source, "조회수 같은 기사1", "요약", "2026-08-18T00:00:00Z", 1L, 50L);
        Article second = article(source, "조회수 같은 기사2", "요약", "2026-08-17T00:00:00Z", 1L, 50L);
        flushAndClear();

        UUID cursorId = List.of(first.getId(), second.getId()).stream()
                .max(Comparator.comparing(UUID::toString))
                .orElseThrow();
        UUID expectedId = List.of(first.getId(), second.getId()).stream()
                .min(Comparator.comparing(UUID::toString))
                .orElseThrow();

        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.VIEW_COUNT,
                null,
                "50_" + cursorId,
                10
        );

        // when
        List<Article> result = articleRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Article::getId)
                .containsExactly(expectedId);
    }

    @Test
    void 발행일_정렬에서_커서_ID가_잘못되면_예외가_발생한다() {
        // given
        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.PUBLISH_DATE,
                Instant.parse("2026-08-18T00:00:00Z"),
                "invalid-uuid",
                10
        );

        // when & then
        // 잘못된 커서를 무시하면 첫 페이지가 다시 조회될 수 있으므로 400으로 이어질 예외를 발생시킵니다.
        assertThatThrownBy(() -> articleRepository.searchByCursor(condition))
                .isInstanceOf(InvalidArticleCursorException.class)
                .hasMessage("Invalid PUBLISH_DATE cursor.");
    }

    @Test
    void 발행일_정렬에서_nextAfter와_nextCursor는_함께_전달되어야_한다() {
        // given
        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.PUBLISH_DATE,
                Instant.parse("2026-08-18T00:00:00Z"),
                null,
                10
        );

        // when & then
        assertThatThrownBy(() -> articleRepository.searchByCursor(condition))
                .isInstanceOf(InvalidArticleCursorException.class)
                .hasMessage("PUBLISH_DATE cursor requires both nextAfter and nextCursor.");
    }

    @Test
    void 댓글수_정렬에서_커서_형식이_잘못되면_예외가_발생한다() {
        // given
        ArticleSearchCondition condition = condition(
                null, null, null, null, null,
                ArticleSortType.COMMENT_COUNT,
                null,
                "invalid-cursor",
                10
        );

        // when & then
        assertThatThrownBy(() -> articleRepository.searchByCursor(condition))
                .isInstanceOf(InvalidArticleCursorException.class)
                .hasMessage("Cursor must be formatted as 'value_articleId'.");
    }

    private ArticleSearchCondition condition(
            String keyword,
            UUID interestId,
            String source,
            Instant from,
            Instant to,
            ArticleSortType sortType,
            Instant nextAfter,
            String nextCursor,
            int size
    ) {
        return new ArticleSearchCondition(
                keyword, interestId, source, from, to,
                sortType, nextAfter, nextCursor, size
        );
    }

    private ArticleSource source(String name) {
        ArticleSource source = ArticleSource.builder()
                .name(name)
                .type(SourceType.RSS)
                .sourceUrl("https://example.com/" + name)
                .build();

        em.persist(source);
        return source;
    }

    private Article article(
            ArticleSource source,
            String title,
            String summary,
            String publishDate,
            Long commentCount,
            Long viewCount
    ) {
        Article article = Article.builder()
                .source(source)
                .sourceUrl("https://example.com/articles/" + title)
                .title(title)
                .publishDate(Instant.parse(publishDate))
                .summary(summary)
                .build();

        // Article builder는 통계값을 기본 0으로 만들기 때문에,
        // 정렬 테스트에 필요한 댓글 수/조회 수만 테스트에서 직접 채웁니다.
        ReflectionTestUtils.setField(article, "commentCount", commentCount);
        ReflectionTestUtils.setField(article, "viewCount", viewCount);

        em.persist(article);
        return article;
    }

    private ArticleInterest articleInterest(Article article, Interest interest) {
        ArticleInterest articleInterest = ArticleInterest.builder()
                .article(article)
                .interest(interest)
                .build();

        em.persist(articleInterest);
        return articleInterest;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
