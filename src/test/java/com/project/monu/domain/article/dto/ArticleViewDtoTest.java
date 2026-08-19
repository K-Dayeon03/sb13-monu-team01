package com.project.monu.domain.article.dto;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.ArticleView;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArticleViewDtoTest {

    @Test
    void ArticleView를_DTO로_변환한다() {
        // given
        // 조회 이력 자체의 값과, 조회 이력에 연결된 기사 값을 각각 준비합니다.
        // ArticleViewDto는 두 엔티티의 값을 한 응답으로 합쳐서 내려주기 때문입니다.
        UUID viewId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-19T00:00:00Z");
        Instant publishDate = Instant.parse("2026-08-18T00:00:00Z");

        ArticleView articleView = mock(ArticleView.class);
        Article article = mock(Article.class);
        ArticleSource source = mock(ArticleSource.class);

        when(articleView.getId()).thenReturn(viewId);
        when(articleView.getViewerId()).thenReturn(viewerId);
        when(articleView.getCreatedAt()).thenReturn(createdAt);
        when(articleView.getArticle()).thenReturn(article);

        when(article.getId()).thenReturn(articleId);
        when(article.getSource()).thenReturn(source);
        when(source.getName()).thenReturn("NAVER");
        when(article.getSourceUrl()).thenReturn("https://example.com/article");
        when(article.getTitle()).thenReturn("기사 제목");
        when(article.getPublishDate()).thenReturn(publishDate);
        when(article.getSummary()).thenReturn("기사 요약");
        when(article.getCommentCount()).thenReturn(10L);
        when(article.getViewCount()).thenReturn(100L);

        // when
        ArticleViewDto dto = ArticleViewDto.from(articleView);

        // then
        // 조회 이력 값은 ArticleView에서, 기사 상세 값은 Article에서 가져왔는지 확인합니다.
        assertThat(dto.id()).isEqualTo(viewId);
        assertThat(dto.viewedBy()).isEqualTo(viewerId);
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.articleId()).isEqualTo(articleId);
        assertThat(dto.source()).isEqualTo("NAVER");
        assertThat(dto.sourceUrl()).isEqualTo("https://example.com/article");
        assertThat(dto.articleTitle()).isEqualTo("기사 제목");
        assertThat(dto.articlePublishedDate()).isEqualTo(publishDate);
        assertThat(dto.articleSummary()).isEqualTo("기사 요약");
        assertThat(dto.articleCommentCount()).isEqualTo(10L);
        assertThat(dto.articleViewCount()).isEqualTo(100L);
    }
}
