package com.project.monu.domain.article.dto;

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
}