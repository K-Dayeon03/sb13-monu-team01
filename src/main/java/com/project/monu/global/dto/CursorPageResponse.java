package com.project.monu.global.dto;

import java.time.Instant;
import java.util.List;

/**
 * 커서 기반 페이지 응답입니다.
 *
 * <p>nextCursor와 nextAfter의 구체적인 의미는 각 목록 API의 정렬 기준에 따라 달라집니다.
 * 이 공통 응답 객체는 페이지 결과를 담는 역할만 하고, 커서 포맷 해석은 도메인별 코드가 담당합니다.</p>
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

    /**
     * 커서 페이지 응답을 만들 때 생성자 대신 사용할 수 있는 정적 팩토리 메서드입니다.
     */
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
