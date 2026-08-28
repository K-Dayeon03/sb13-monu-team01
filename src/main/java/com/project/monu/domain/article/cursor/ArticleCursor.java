package com.project.monu.domain.article.cursor;

import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.exception.InvalidArticleCursorException;

import java.time.Instant;
import java.util.UUID;

/**
 * 기사 목록 커서의 생성과 파싱을 담당합니다.
 *
 * <p>PUBLISH_DATE 정렬은 발행일을 nextAfter에 담고, 같은 발행일 안에서의 위치를
 * nextCursor의 기사 ID로 구분합니다. COMMENT_COUNT, VIEW_COUNT 정렬은 정렬값과
 * 기사 ID를 함께 담아 같은 정렬값이 여러 개 있어도 다음 페이지가 안정적으로 이어지게 합니다.</p>
 */
public final class ArticleCursor {

    private static final String VALUE_ID_DELIMITER = "_";

    private ArticleCursor() {
    }

    /**
     * 응답에 내려줄 다음 페이지 커서를 만듭니다.
     *
     * <p>마지막으로 응답한 기사를 기준점으로 삼아야 다음 요청에서 중복 없이
     * 그 다음 기사부터 조회할 수 있습니다.</p>
     */
    public static String createNextCursor(Article article, ArticleSortType sortType) {
        if (article == null) {
            return null;
        }

        return switch (ArticleSortType.resolve(sortType)) {
            case COMMENT_COUNT -> article.getCommentCount() + VALUE_ID_DELIMITER + article.getId();
            case VIEW_COUNT -> article.getViewCount() + VALUE_ID_DELIMITER + article.getId();
            case PUBLISH_DATE -> article.getId().toString();
        };
    }

    /**
     * 발행일 정렬 커서에서 기사 ID를 꺼냅니다.
     *
     * <p>발행일 정렬은 nextAfter와 nextCursor가 한 쌍입니다. 둘 중 하나만 있으면
     * 기준 시각 또는 같은 시각 내 위치가 빠진 상태라 페이지가 반복되거나 건너뛸 수 있습니다.</p>
     */
    public static UUID parsePublishDateCursor(Instant nextAfter, String nextCursor) {
        boolean hasNextAfter = nextAfter != null;
        boolean hasNextCursor = hasText(nextCursor);

        if (!hasNextAfter && !hasNextCursor) {
            return null;
        }

        if (!hasNextAfter || !hasNextCursor) {
            throw new InvalidArticleCursorException(
                    "PUBLISH_DATE cursor requires both nextAfter and nextCursor."
            );
        }

        try {
            return UUID.fromString(nextCursor);
        } catch (IllegalArgumentException e) {
            throw new InvalidArticleCursorException("Invalid PUBLISH_DATE cursor.");
        }
    }

    /**
     * 댓글 수/조회 수 정렬에 쓰는 "정렬값_기사ID" 형태의 커서를 파싱합니다.
     */
    public static NumericCursor parseNumericCursor(String nextCursor) {
        if (!hasText(nextCursor)) {
            return null;
        }

        int delimiterIndex = nextCursor.lastIndexOf(VALUE_ID_DELIMITER);
        if (delimiterIndex < 0) {
            throw new InvalidArticleCursorException(
                    "Cursor must be formatted as 'value_articleId'."
            );
        }

        String valuePart = nextCursor.substring(0, delimiterIndex);
        String articleIdPart = nextCursor.substring(delimiterIndex + 1);

        try {
            return new NumericCursor(
                    Long.parseLong(valuePart),
                    UUID.fromString(articleIdPart)
            );
        } catch (IllegalArgumentException e) {
            throw new InvalidArticleCursorException("Invalid cursor value.");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 숫자 정렬 커서는 정렬값과 보조 정렬값인 기사 ID를 함께 보관합니다.
     */
    public record NumericCursor(long value, UUID articleId) {
    }
}
