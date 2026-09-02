package com.project.monu.domain.article.dto.request;

/**
 * 기사 목록에서 지원하는 정렬 기준입니다.
 *
 * <p>커서 응답 규칙은 정렬 기준마다 다릅니다.</p>
 * <ul>
 *     <li>PUBLISH_DATE: nextAfter = 마지막 기사 publishDate, nextCursor = 마지막 기사 id</li>
 *     <li>COMMENT_COUNT: nextCursor = 마지막 기사 commentCount + "_" + 마지막 기사 id</li>
 *     <li>VIEW_COUNT: nextCursor = 마지막 기사 viewCount + "_" + 마지막 기사 id</li>
 * </ul>
 */
public enum ArticleSortType {
    PUBLISH_DATE,
    COMMENT_COUNT,
    VIEW_COUNT;

    public static ArticleSortType resolve(ArticleSortType sortType) {
        return sortType == null ? PUBLISH_DATE : sortType;
    }

    public static ArticleSortType from(String orderBy) {
        if (orderBy == null || orderBy.isBlank()) {
            return PUBLISH_DATE;
        }

        return switch (orderBy) {
            case "publishDate" -> PUBLISH_DATE;
            case "commentCount" -> COMMENT_COUNT;
            case "viewCount" -> VIEW_COUNT;
            default -> throw new IllegalArgumentException("Invalid article orderBy: " + orderBy);
        };
    }
}
