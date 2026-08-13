package com.project.monu.domain.article.dto;

// ArticleView 엔티티가 만들어지면 아래 import를 사용하면 됩니다.
// import com.project.monu.domain.article.entity.Article;
// import com.project.monu.domain.article.entity.ArticleView;

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

    /*
     * ArticleView 엔티티를 ArticleViewDto로 변환하는 정적 팩토리 메서드입니다.
     * ArticleView는 조회 이력 정보를 가지고 있고,
     * 실제 기사 상세 정보는 연결된 Article 엔티티에서 꺼내서 DTO에 담습니다.
     *
     * ArticleView 엔티티가 완성되면 주석을 해제해서 사용하면 됩니다.
     */
    /*
    public static ArticleViewDto from(ArticleView articleView) {
        Article article = articleView.getArticle();

        return new ArticleViewDto(
                articleView.getId(),
                articleView.getViewedBy(),
                articleView.getCreatedAt(),
                article.getId(),
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishedDate(),
                article.getSummary(),
                article.getCommentCount(),
                article.getViewCount()
        );
    }
    */
}