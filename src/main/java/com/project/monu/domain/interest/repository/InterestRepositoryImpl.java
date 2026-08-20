package com.project.monu.domain.interest.repository;

import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.QInterest;
import com.project.monu.domain.interest.exception.InvalidInterestCursorException;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Interest> searchByCursor(InterestSearchCondition condition) {
        QInterest interest = QInterest.interest;

        return queryFactory
                .selectFrom(interest)
                .where(
                        nameContains(condition.keyword()),
                        cursorCondition(condition)
                )
                .orderBy(orderBy(condition))
                .limit(condition.size() + 1)
                .fetch();
    }

    @Override
    public long countByCondition(InterestSearchCondition condition) {
        QInterest interest = QInterest.interest;

        Long count = queryFactory
                .select(interest.count())
                .from(interest)
                .where(nameContains(condition.keyword()))
                .fetchOne();

        return count == null ? 0L : count;
    }

    private BooleanExpression nameContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return QInterest.interest.name.containsIgnoreCase(keyword);
    }

    private OrderSpecifier<?>[] orderBy(InterestSearchCondition condition) {
        QInterest interest = QInterest.interest;
        InterestSortType sortType = condition.sortType() == null
                ? InterestSortType.SUBSCRIBER_COUNT
                : condition.sortType();

        return switch (sortType) {
            case SUBSCRIBER_COUNT -> new OrderSpecifier[]{
                    interest.subscriberCount.desc(),
                    interest.id.desc()
            };
            case NAME -> new OrderSpecifier[]{
                    interest.name.asc(),
                    interest.id.asc()
            };
        };
    }

    private BooleanExpression cursorCondition(InterestSearchCondition condition) {
        String nextCursor = condition.nextCursor();
        if (nextCursor == null || nextCursor.isBlank()) {
            return null;
        }

        InterestSortType sortType = condition.sortType() == null
                ? InterestSortType.SUBSCRIBER_COUNT
                : condition.sortType();

        int lastUnderscoreIndex = nextCursor.lastIndexOf('_');
        if (lastUnderscoreIndex < 0) {
            throw new InvalidInterestCursorException("Cursor must be formatted as 'value_interestId'.");
        }

        String valuePart = nextCursor.substring(0, lastUnderscoreIndex);
        String idPart = nextCursor.substring(lastUnderscoreIndex + 1);

        UUID cursorId;
        try {
            cursorId = UUID.fromString(idPart);
        } catch (IllegalArgumentException e) {
            throw new InvalidInterestCursorException("Invalid cursor id.");
        }

        QInterest interest = QInterest.interest;

        return switch (sortType) {
            case SUBSCRIBER_COUNT -> {
                long cursorValue;
                try {
                    cursorValue = Long.parseLong(valuePart);
                } catch (NumberFormatException e) {
                    throw new InvalidInterestCursorException("Invalid cursor value.");
                }
                yield interest.subscriberCount.lt(cursorValue)
                        .or(interest.subscriberCount.eq(cursorValue)
                                .and(interest.id.lt(cursorId)));
            }
            case NAME -> interest.name.gt(valuePart)
                    .or(interest.name.eq(valuePart)
                            .and(interest.id.gt(cursorId)));
        };
    }
}