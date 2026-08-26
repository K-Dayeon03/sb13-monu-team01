package com.project.monu.domain.article.collector.naver.dto;

public record NaverErrorResponse (
        String errorCode,
        String errorMessage
) {
}
