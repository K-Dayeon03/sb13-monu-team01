package com.project.monu.domain.article.dto.request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;

/**
 * 기사 목록 조회에 사용하는 검색/필터/커서 조건입니다.
 *
 * @param keyword 제목 또는 요약에서 부분 일치로 검색할 키워드
 * @param interestId 관심사 ID 필터
 * @param sourceIn 포함할 출처 이름 필터
 * @param publishDateFrom 발행일 시작 범위
 * @param publishDateTo 발행일 종료 범위
 * @param orderBy 정렬 기준
 * @param direction 정렬 방향
 * @param after 다음 페이지 조회 기준이 되는 마지막 정렬 기준값
 * @param cursor 다음 페이지 조회 기준이 되는 보조 커서
 * @param limit 페이지 크기
 */
public record ArticleSearchCondition(
        String keyword,
        UUID interestId,
        List<String> sourceIn,
        Instant publishDateFrom,
        Instant publishDateTo,
        ArticleSortType orderBy,
        Sort.Direction direction,
        Instant after,
        String cursor,
        int limit
) {
}
