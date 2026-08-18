package com.project.monu.domain.article.collector.dto;

import java.time.Instant;

public record CollectedArticle(
        String title, // 제목
        String originalLink, // 원본 링크
        String summary, // 정리된 요약
        Instant publishedAt // 변환된 발행 시각
) {
}
