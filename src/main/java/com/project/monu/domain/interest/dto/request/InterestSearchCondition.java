package com.project.monu.domain.interest.dto.request;

/**
 * 관심사 목록 조회에 사용하는 검색/정렬/커서 조건입니다.
 *
 * @param keyword 이름에서 부분 일치로 검색할 키워드
 * @param sortType 정렬 기준
 * @param nextCursor 다음 페이지 조회 기준 커서 ("정렬값_id" 형식)
 * @param size 페이지 크기
 */
public record InterestSearchCondition(
        String keyword,
        InterestSortType sortType,
        String nextCursor,
        int size
) {
}