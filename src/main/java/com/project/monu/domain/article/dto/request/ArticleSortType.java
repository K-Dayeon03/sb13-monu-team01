package com.project.monu.domain.article.dto.request;
/*
* PUBLISH_DATE 정렬
- nextAfter = 마지막 기사 publishDate
- nextCursor = 마지막 기사 id

COMMENT_COUNT 정렬
- nextCursor = 마지막 기사 commentCount + "_" + 마지막 기사 id
- nextAfter = 마지막 기사 publishDate

VIEW_COUNT 정렬
- nextCursor = 마지막 기사 viewCount + "_" + 마지막 기사 id
- nextAfter = 마지막 기사 publishDate
*
* */
public enum ArticleSortType {
    PUBLISH_DATE,
    COMMENT_COUNT,
    VIEW_COUNT
}
