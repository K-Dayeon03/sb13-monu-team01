package com.project.monu.domain.article.entity;


/**
 * 기사 출처의 수집 방식 유형
 * API - Naver 검색 API처럼 JSON 응답을 주는 출처
 * RSS - 한국경제/조선일보/연합뉴스처럼 RSS(XML) 피드를 주는 출처
 */
public enum SourceType {
    API,
    RSS
}
