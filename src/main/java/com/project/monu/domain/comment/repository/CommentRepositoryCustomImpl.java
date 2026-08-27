package com.project.monu.domain.comment.repository;

import com.project.monu.domain.comment.dto.request.CommentSearchCondition;
import com.project.monu.domain.comment.entity.QComment;
import com.project.monu.domain.comment.entity.QCommentLike;
import com.project.monu.domain.comment.exception.InvalidCommentCursorException;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommentQueryResult> searchByCursor(CommentSearchCondition condition) {
        return switch (condition.sortType()) {
            case CREATED_AT -> searchByCreatedAt(condition);
            case LIKE_COUNT -> searchByLikeCount(condition);
        };
    }

    private List<CommentQueryResult> searchByCreatedAt(CommentSearchCondition condition) {
        QComment comment = QComment.comment;
        QCommentLike commentLike = new QCommentLike("commentLike");
        QCommentLike myLike = new QCommentLike("myLike");

        NumberExpression<Long> likeCount = commentLike.id.count();

        List<Tuple> rows = queryFactory
                .select(comment, likeCount, myLike.id)
                .from(comment)
                .leftJoin(commentLike).on(commentLike.comment.eq(comment))
                .leftJoin(myLike).on(
                        myLike.comment.eq(comment)
                                .and(myLike.likedBy.id.eq(condition.requestUserId()))
                )
                .where(
                        comment.deletedAt.isNull(),
                        articleEq(condition.articleId()),
                        createdAtCursorCondition(condition)
                )
                .groupBy(comment, myLike.id)
                .orderBy(createdAtOrderBy(condition))
                .limit(condition.limit() + 1L)
                .fetch();

        return toResults(rows, comment, likeCount, myLike);
    }

    private List<CommentQueryResult> searchByLikeCount(CommentSearchCondition condition) {
        QComment comment = QComment.comment;
        QCommentLike commentLike = new QCommentLike("commentLike");
        QCommentLike myLike = new QCommentLike("myLike");

        NumberExpression<Long> likeCount = commentLike.id.count();
        BooleanExpression cursorCondition = likeCountCursorCondition(condition, likeCount);

        JPAQuery<Tuple> query = queryFactory
                .select(comment, likeCount, myLike.id)
                .from(comment)
                .leftJoin(commentLike).on(commentLike.comment.eq(comment))
                .leftJoin(myLike).on(
                        myLike.comment.eq(comment)
                                .and(myLike.likedBy.id.eq(condition.requestUserId()))
                )
                .where(
                        comment.deletedAt.isNull(),
                        articleEq(condition.articleId())
                )
                .groupBy(comment, myLike.id);

        if (cursorCondition != null) {
            query.having(cursorCondition);
        }

        List<Tuple> rows = query
                .orderBy(likeCountOrderBy(condition, likeCount))
                .limit(condition.limit() + 1L)
                .fetch();

        return toResults(rows, comment, likeCount, myLike);
    }

    @Override
    public long countByCondition(CommentSearchCondition condition) {
        QComment comment = QComment.comment;

        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.deletedAt.isNull(),
                        articleEq(condition.articleId())
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    private BooleanExpression articleEq(UUID articleId) {
        if (articleId == null) {
            return null;
        }

        return QComment.comment.article.id.eq(articleId);
    }

    private OrderSpecifier<?>[] createdAtOrderBy(CommentSearchCondition condition) {
        QComment comment = QComment.comment;
        boolean desc = condition.direction().isDescending();

        return desc
                ? new OrderSpecifier[]{
                comment.createdAt.desc(),
                comment.id.desc()
        }
                : new OrderSpecifier[]{
                comment.createdAt.asc(),
                comment.id.asc()
        };
    }

    private OrderSpecifier<?>[] likeCountOrderBy(
            CommentSearchCondition condition,
            NumberExpression<Long> likeCount
    ) {
        QComment comment = QComment.comment;
        boolean desc = condition.direction().isDescending();

        return desc
                ? new OrderSpecifier[]{
                likeCount.desc(),
                comment.createdAt.desc(),
                comment.id.desc()
        }
                : new OrderSpecifier[]{
                likeCount.asc(),
                comment.createdAt.asc(),
                comment.id.asc()
        };
    }

    private BooleanExpression createdAtCursorCondition(CommentSearchCondition condition) {
        if (condition.cursor() == null || condition.cursor().isBlank()) {
            return null;
        }

        QComment comment = QComment.comment;
        Cursor cursor = parseCursor(condition.cursor());

        Instant cursorCreatedAt;
        try {
            cursorCreatedAt = Instant.parse(cursor.value());
        } catch (Exception e) {
            throw new InvalidCommentCursorException();
        }

        boolean desc = condition.direction().isDescending();

        BooleanExpression idTieBreak =
                desc ? comment.id.lt(cursor.id()) : comment.id.gt(cursor.id());

        return desc
                ? comment.createdAt.lt(cursorCreatedAt)
                  .or(comment.createdAt.eq(cursorCreatedAt).and(idTieBreak))
                : comment.createdAt.gt(cursorCreatedAt)
                  .or(comment.createdAt.eq(cursorCreatedAt).and(idTieBreak));
    }

    private BooleanExpression likeCountCursorCondition(
            CommentSearchCondition condition,
            NumberExpression<Long> likeCount
    ) {
        if (condition.cursor() == null || condition.cursor().isBlank()) {
            return null;
        }

        QComment comment = QComment.comment;
        Cursor cursor = parseCursor(condition.cursor());

        long cursorLikeCount;
        try {
            cursorLikeCount = Long.parseLong(cursor.value());
        } catch (NumberFormatException e) {
            throw new InvalidCommentCursorException();
        }

        boolean desc = condition.direction().isDescending();

        BooleanExpression idTieBreak =
                desc ? comment.id.lt(cursor.id()) : comment.id.gt(cursor.id());

        Instant after = condition.after();

        BooleanExpression secondaryTieBreak = after == null
                ? idTieBreak
                : desc
                  ? comment.createdAt.lt(after)
                    .or(comment.createdAt.eq(after).and(idTieBreak))
                  : comment.createdAt.gt(after)
                    .or(comment.createdAt.eq(after).and(idTieBreak));

        return desc
                ? likeCount.lt(cursorLikeCount)
                  .or(likeCount.eq(cursorLikeCount).and(secondaryTieBreak))
                : likeCount.gt(cursorLikeCount)
                  .or(likeCount.eq(cursorLikeCount).and(secondaryTieBreak));
    }

    private Cursor parseCursor(String cursor) {
        int lastUnderscoreIndex = cursor.lastIndexOf('_');

        if (lastUnderscoreIndex < 0) {
            throw new InvalidCommentCursorException();
        }

        String valuePart = cursor.substring(0, lastUnderscoreIndex);
        String idPart = cursor.substring(lastUnderscoreIndex + 1);

        UUID cursorId;
        try {
            cursorId = UUID.fromString(idPart);
        } catch (IllegalArgumentException e) {
            throw new InvalidCommentCursorException();
        }

        return new Cursor(valuePart, cursorId);
    }

    private List<CommentQueryResult> toResults(
            List<Tuple> rows,
            QComment comment,
            NumberExpression<Long> likeCount,
            QCommentLike myLike
    ) {
        return rows.stream()
                .map(row -> new CommentQueryResult(
                        row.get(comment),
                        row.get(likeCount) == null ? 0L : row.get(likeCount),
                        row.get(myLike.id) != null
                ))
                .toList();
    }

    private record Cursor(
            String value,
            UUID id
    ) {
    }
}