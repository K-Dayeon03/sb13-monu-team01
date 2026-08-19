package com.project.monu.domain.article.dto.request;

import java.time.Instant;
import java.util.UUID;

/**
 * 기사 목록 조회에 사용하는 검색/필터/커서 조건입니다.
 *
 * @param keyword 제목 또는 요약에서 부분 일치로 검색할 키워드
 * @param interestId 관심사 ID 필터
 * @param source 출처 이름 필터
 * @param publishDateFrom 발행일 시작 범위
 * @param publishDateTo 발행일 종료 범위
 * @param sortType 정렬 기준
 * @param nextAfter 다음 페이지 조회 기준이 되는 마지막 발행 시각
 * @param nextCursor 다음 페이지 조회 기준이 되는 보조 커서
 * @param size 페이지 크기
 */
public record ArticleSearchCondition(
        String keyword,
        UUID interestId,
        String source,
        Instant publishDateFrom,
        Instant publishDateTo,
        ArticleSortType sortType,
        Instant nextAfter,
        String nextCursor,
        int size
) {
}
