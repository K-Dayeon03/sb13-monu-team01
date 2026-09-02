package com.project.monu.domain.interest.dto.request;

import org.springframework.data.domain.Sort;

import java.time.Instant;

/**
 * 관심사 목록 조회에 사용하는 검색/정렬/커서 조건입니다.
 *
 * @param keyword 이름에서 부분 일치로 검색할 키워드
 * @param sortType 정렬 기준
 * @param direction 정렬 방향 (ASC, DESC)
 * @param nextCursor 다음 페이지 조회 기준 커서 ("정렬값_id" 형식)
 * @param nextAfter 보조 커서(마지막으로 조회된 관심사의 createdAt), 같은 정렬값이 여러 개일 때 순서를 안정적으로 유지하기 위해 사용
 * @param size 페이지 크기
 */
public record InterestSearchCondition(
        String keyword,
        InterestSortType sortType,
        Sort.Direction direction,
        String nextCursor,
        Instant nextAfter,
        int size
) {
}
