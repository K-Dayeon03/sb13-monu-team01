package com.project.monu.domain.article.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {


    private Article createArticle() {
        return Article.builder()
                .source(null)
                .sourceUrl("https://example.com/1")
                .title("테스트 기사")
                .publishDate(Instant.parse("2026-08-21T00:00:00Z"))
                .summary("테스트 요약")
                .build();
    }

    @Test
    @DisplayName("논리 삭제하면 deletedAt이 채워지고 isDeleted가 true가 된다")
    void 기사를_논리_삭제한다() {
        // given
        Article article = createArticle();

        // when
        article.softDelete();

        // then
        assertThat(article.getDeletedAt()).isNotNull();
        assertThat(article.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("조회수를 1 증가시킨다")
    void 조회수를_증가시킨다() {
        // given
        Article article = createArticle();

        // when
        article.increaseViewCount();

        // then
        assertThat(article.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("댓글수를 1 증가시킨다")
    void 댓글수를_증가시킨다() {
        // given
        Article article = createArticle();

        // when
        article.increaseCommentCount();

        // then
        assertThat(article.getCommentCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("댓글수를 1 감소시킨다")
    void 댓글수를_감소시킨다() {
        // given
        Article article = createArticle();
        article.increaseCommentCount();

        // when
        article.decreaseCommentCount();

        // then
        assertThat(article.getCommentCount()).isZero();
    }

    @Test
    @DisplayName("댓글수는 0보다 작아지지 않는다")
    void 댓글수는_음수가_되지_않는다() {
        // given
        Article article = createArticle();

        // when
        article.decreaseCommentCount();

        // then
        assertThat(article.getCommentCount()).isZero();
    }

}
