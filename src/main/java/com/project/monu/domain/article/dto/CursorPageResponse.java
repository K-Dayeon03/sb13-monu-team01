package com.project.monu.domain.article.dto;

import java.time.Instant;
import java.util.List;

/**
 * 커서 기반 페이지 응답
 *
 * @param content 페이지 내용
 * @param nextCursor 다음 페이지 커서
 * @param nextAfter 다음 보조 커서
 * @param size 페이지 크기
 * @param totalElements 총 요소 수
 * @param hasNext 다음 페이지 존재 여부
 */


public record CursorPageResponse<T>(

        List<T> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {

    public static <T> CursorPageResponse<T> of(
            List<T> content,
            String nextCursor,
            Instant nextAfter,
            int size,
            long totalElements,
            boolean hasNext
    ) {
        return new CursorPageResponse<>(content, nextCursor, nextAfter,
                size, totalElements, hasNext);
    }
}
