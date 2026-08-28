package com.project.monu.domain.article.dto.response;

import com.project.monu.domain.article.entity.Article;

import java.time.Instant;
import java.util.UUID;

/**
 * 뉴스 기사 목록/상세 응답에 사용하는 DTO입니다.
 */
public record ArticleDto(
        // 기사 기본 정보
        UUID id,
        String source,
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,

        // 기사 통계 정보
        Long commentCount,
        Long viewCount,

        // 현재 로그인 사용자의 조회 여부
        Boolean viewedByMe
) {

    /**
     * Article 엔티티와 현재 사용자 기준 조회 여부를 조합해 응답 DTO를 만듭니다.
     */
    public static ArticleDto from(Article article, boolean viewedByMe) {
        return new ArticleDto(
                article.getId(),
                article.getSource().getName(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                article.getCommentCount(),
                article.getViewCount(),
                viewedByMe
        );
    }
}
