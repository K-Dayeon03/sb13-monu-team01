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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    private InterestSortType resolveSortType(InterestSortType sortType) {
        return sortType == null ? InterestSortType.SUBSCRIBER_COUNT : sortType;
    }

    private boolean resolveDescending(Sort.Direction direction) {
        return direction == null || direction.isDescending();
    }

    // 정렬 기준(subscriberCount/name)과 방향(ASC/DESC)에 따라 정렬 조건을 만듭니다.
    // createdAt, id를 보조 정렬로 사용해 같은 정렬값이 있어도 순서를 안정적으로 유지합니다.
    private OrderSpecifier<?>[] orderBy(InterestSearchCondition condition) {
        QInterest interest = QInterest.interest;
        InterestSortType sortType = resolveSortType(condition.sortType());
        boolean desc = resolveDescending(condition.direction());

        return switch (sortType) {
            case SUBSCRIBER_COUNT -> desc
                    ? new OrderSpecifier[]{interest.subscriberCount.desc(), interest.createdAt.desc(), interest.id.desc()}
                    : new OrderSpecifier[]{interest.subscriberCount.asc(), interest.createdAt.asc(), interest.id.asc()};
            case NAME -> desc
                    ? new OrderSpecifier[]{interest.name.desc(), interest.createdAt.desc(), interest.id.desc()}
                    : new OrderSpecifier[]{interest.name.asc(), interest.createdAt.asc(), interest.id.asc()};
        };
    }

    private BooleanExpression cursorCondition(InterestSearchCondition condition) {
        String nextCursor = condition.nextCursor();
        if (nextCursor == null || nextCursor.isBlank()) {
            return null;
        }

        InterestSortType sortType = resolveSortType(condition.sortType());
        boolean desc = resolveDescending(condition.direction());

        int lastUnderscoreIndex = nextCursor.lastIndexOf('_');
        if (lastUnderscoreIndex < 0) {
            throw new InvalidInterestCursorException();
        }

        String valuePart = nextCursor.substring(0, lastUnderscoreIndex);
        String idPart = nextCursor.substring(lastUnderscoreIndex + 1);

        UUID cursorId;
        try {
            cursorId = UUID.fromString(idPart);
        } catch (IllegalArgumentException e) {
            throw new InvalidInterestCursorException();
        }

        QInterest interest = QInterest.interest;
        Instant after = condition.nextAfter();

        BooleanExpression idTieBreak = desc ? interest.id.lt(cursorId) : interest.id.gt(cursorId);
        BooleanExpression secondaryTieBreak = after == null
                ? idTieBreak
                : desc
                        ? interest.createdAt.lt(after).or(interest.createdAt.eq(after).and(idTieBreak))
                        : interest.createdAt.gt(after).or(interest.createdAt.eq(after).and(idTieBreak));

        return switch (sortType) {
            case SUBSCRIBER_COUNT -> {
                long cursorValue;
                try {
                    cursorValue = Long.parseLong(valuePart);
                } catch (NumberFormatException e) {
                    throw new InvalidInterestCursorException();
                }
                yield desc
                        ? interest.subscriberCount.lt(cursorValue)
                                .or(interest.subscriberCount.eq(cursorValue).and(secondaryTieBreak))
                        : interest.subscriberCount.gt(cursorValue)
                                .or(interest.subscriberCount.eq(cursorValue).and(secondaryTieBreak));
            }
            case NAME -> desc
                    ? interest.name.lt(valuePart).or(interest.name.eq(valuePart).and(secondaryTieBreak))
                    : interest.name.gt(valuePart).or(interest.name.eq(valuePart).and(secondaryTieBreak));
        };
    }
}
