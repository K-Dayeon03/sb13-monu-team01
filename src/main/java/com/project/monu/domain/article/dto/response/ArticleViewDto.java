package com.project.monu.domain.article.dto.response;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleView;

import java.time.Instant;
import java.util.UUID;

public record ArticleViewDto(
        // 기사 조회 이력 정보
        UUID id,
        UUID viewedBy,
        Instant createdAt,

        // 조회한 기사 정보
        UUID articleId,
        String source,
        String sourceUrl,
        String articleTitle,
        Instant articlePublishedDate,
        String articleSummary,
        Long articleCommentCount,
        Long articleViewCount
) {

    /**
     * ArticleView 엔티티를 ArticleViewDto로 변환합니다.
     *
     * ArticleView는 "누가 언제 어떤 기사를 조회했는지"를 가지고 있고,
     * 실제 기사 응답 정보는 연결된 Article 엔티티에서 꺼내 DTO에 담습니다.
     */
    public static ArticleViewDto from(ArticleView articleView) {
        Article article = articleView.getArticle();

        return new ArticleViewDto(
                articleView.getId(),
                articleView.getViewer().getId(),
                articleView.getCreatedAt(),
                article.getId(),
                article.getSource().getDisplayName(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                article.getCommentCount(),
                article.getViewCount()
        );
    }
}