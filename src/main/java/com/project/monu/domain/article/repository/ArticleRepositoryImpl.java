package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.cursor.ArticleCursor;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.QArticle;
import com.project.monu.domain.article.entity.QArticleInterest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 검색 조건과 커서를 기준으로 기사 목록을 조회합니다.
     * size + 1개를 조회해서 다음 페이지 존재 여부를 판단합니다.
     *
     * 정렬 기준에 따라 발행일, 댓글 수, 조회 수 기준 커서를 적용합니다.
     */
    private BooleanExpression cursorCondition(ArticleSearchCondition condition) {
        ArticleSortType sortType = ArticleSortType.resolve(condition.orderBy());

        return switch (sortType) {
            case COMMENT_COUNT -> commentCountCursor(condition.cursor(), condition.direction());
            case VIEW_COUNT -> viewCountCursor(condition.cursor(), condition.direction());
            case PUBLISH_DATE -> publishDateCursor(condition.after(), condition.cursor(), condition.direction());
        };
    }

    private BooleanExpression viewCountCursor(String nextCursor, Sort.Direction direction) {
        return numericCursor(nextCursor, direction, QArticle.article.viewCount);
    }

    private BooleanExpression commentCountCursor(String nextCursor, Sort.Direction direction) {
        return numericCursor(nextCursor, direction, QArticle.article.commentCount);
    }

    /**
     * 댓글 수/조회 수처럼 숫자 컬럼을 기준으로 정렬할 때의 커서 조건입니다.
     *
     * <p>정렬값이 같은 기사가 여러 개 있을 수 있으므로 article.id를 보조 조건으로 함께 비교합니다.
     * 이 보조 조건이 있어야 페이지를 넘겨도 같은 기사가 반복되지 않고 순서도 안정적으로 유지됩니다.</p>
     */
    private BooleanExpression numericCursor(
            String nextCursor,
            Sort.Direction direction,
            NumberExpression<Long> sortField
    ) {
        ArticleCursor.NumericCursor cursor = ArticleCursor.parseNumericCursor(nextCursor);

        if (cursor == null) {
            return null;
        }

        QArticle article = QArticle.article;

        if (isAscending(direction)) {
            return sortField.gt(cursor.value())
                    .or(sortField.eq(cursor.value())
                            .and(article.id.gt(cursor.articleId())));
        }

        return sortField.lt(cursor.value())
                .or(sortField.eq(cursor.value())
                        .and(article.id.lt(cursor.articleId())));
    }

    @Override
    public List<Article> searchByCursor(ArticleSearchCondition condition) {
        QArticle article = QArticle.article;

        return queryFactory
                .selectFrom(article)
                // ArticleDto.source 값을 만들 때 추가 조회가 발생하지 않도록 출처를 함께 조회합니다.
                .leftJoin(article.source).fetchJoin()
                // QueryDSL은 where에 null 조건이 들어오면 해당 조건을 무시합니다.
                // 그래서 각 조건 메서드는 값이 없을 때 null을 반환하도록 만들었습니다.
                .where(articleSearchConditions(condition))
                // 기본 정렬: 최신 기사순.
                // 같은 발행 시각의 기사가 여러 개일 수 있으므로 id를 보조 정렬로 사용합니다.
                .orderBy(orderBy(condition))
                .limit(condition.limit() + 1)
                .fetch();
    }

    /**
     * 현재 검색 조건에 해당하는 전체 기사 수를 조회합니다.
     * 커서 조건은 제외하고 필터 조건만 적용합니다.
     *
     * count는 "현재 필터에 해당하는 전체 개수"를 의미하므로
     * nextAfter 같은 페이지 이동용 커서 조건은 넣지 않습니다.
     */
    @Override
    public long countByCondition(ArticleSearchCondition condition) {
        QArticle article = QArticle.article;

        Long count = queryFactory
                .select(article.count())
                .from(article)
                .where(articleCountConditions(condition))
                .fetchOne();

        return count == null ? 0L : count;
    }

    /**
     * 목록 조회와 전체 개수 조회가 공유하는 필터 조건입니다.
     *
     * <p>커서 조건은 현재 페이지 위치만 의미하므로 여기에 넣지 않습니다.
     * 그래야 totalElements가 "현재 필터 전체 건수"라는 의미를 유지합니다.</p>
     */
    private Predicate[] articleSearchConditions(ArticleSearchCondition condition) {
        List<Predicate> conditions = new ArrayList<>(articleFilterConditions(condition));
        conditions.add(cursorCondition(condition));
        conditions.add(notDeleted());

        return conditions.toArray(Predicate[]::new);
    }

    private Predicate[] articleCountConditions(ArticleSearchCondition condition) {
        List<Predicate> conditions = new ArrayList<>(articleFilterConditions(condition));
        conditions.add(notDeleted());

        return conditions.toArray(Predicate[]::new);
    }

    private List<Predicate> articleFilterConditions(ArticleSearchCondition condition) {
        return Arrays.asList(
                keywordContains(condition.keyword()),
                sourceIn(condition.sourceIn()),
                interestEq(condition.interestId()),
                publishDateGoe(condition.publishDateFrom()),
                publishDateLoe(condition.publishDateTo())
        );
    }

    // 검색어가 있으면 제목 또는 요약 중 하나라도 부분 일치하는 기사를 조회합니다.
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QArticle.article.title.containsIgnoreCase(keyword)
                .or(QArticle.article.summary.containsIgnoreCase(keyword));
    }

    // 출처 이름 목록이 있으면 해당 출처 중 하나에 포함되는 기사만 조회합니다.
    private BooleanExpression sourceIn(List<String> sourceIn) {
        if (sourceIn == null || sourceIn.isEmpty()) {
            return null;
        }

        List<String> filteredSources = sourceIn.stream()
                .filter(source -> source != null && !source.isBlank())
                .toList();

        if (filteredSources.isEmpty()) {
            return null;
        }

        return QArticle.article.source.displayName.in(filteredSources)
                .or(QArticle.article.source.name.in(filteredSources));
    }

    // 관심사 ID가 있으면 해당 관심사와 연결된 기사만 조회합니다.
    // ArticleInterest 매핑 테이블에 해당 article + interestId 조합이 존재하는지 exists로 확인합니다.
    private BooleanExpression interestEq(UUID interestId) {
        if (interestId == null) {
            return null;
        }

        QArticle article = QArticle.article;
        QArticleInterest articleInterest = QArticleInterest.articleInterest;

        return JPAExpressions
                .selectOne()
                .from(articleInterest)
                .where(
                        articleInterest.article.eq(article),
                        articleInterest.interest.id.eq(interestId)
                )
                .exists();
    }

    // 발행일 시작 조건입니다.
    private BooleanExpression publishDateGoe(Instant publishDateFrom) {
        if (publishDateFrom == null) {
            return null;
        }

        return QArticle.article.publishDate.goe(publishDateFrom);
    }

    // 발행일 종료 조건입니다.
    private BooleanExpression publishDateLoe(Instant publishDateTo) {
        if (publishDateTo == null) {
            return null;
        }

        return QArticle.article.publishDate.loe(publishDateTo);
    }

    // 다음 페이지 조회 시 마지막으로 본 발행일보다 이전 기사만 조회합니다.
    // 최신순(desc) 정렬이므로 다음 페이지는 nextAfter보다 과거 발행일을 가져와야 합니다.
    // 발행일 정렬 커서는 nextAfter(발행일)와 nextCursor(기사 ID)가 한 쌍으로 들어와야 합니다.
    private BooleanExpression publishDateCursor(Instant nextAfter, String nextCursor, Sort.Direction direction) {
        UUID cursorId = ArticleCursor.parsePublishDateCursor(nextAfter, nextCursor);
        if (cursorId == null) {
            return null;
        }

        QArticle article = QArticle.article;

        if (isAscending(direction)) {
            return article.publishDate.gt(nextAfter)
                    .or(article.publishDate.eq(nextAfter)
                            .and(article.id.gt(cursorId)));
        }

        return article.publishDate.lt(nextAfter)
                .or(article.publishDate.eq(nextAfter)
                        .and(article.id.lt(cursorId)));
    }

    // 논리 삭제되지 않은 기사만 조회합니다.
    private BooleanExpression notDeleted() {
        return QArticle.article.deletedAt.isNull();
    }

    // 요청된 정렬 기준에 따라 정렬 조건을 선택합니다.
    // 모든 정렬은 id를 보조 정렬로 사용해 같은 값이 있는 경우에도 순서를 안정적으로 유지합니다.
    private OrderSpecifier<?>[] orderBy(ArticleSearchCondition condition) {
        QArticle article = QArticle.article;

        ArticleSortType sortType = ArticleSortType.resolve(condition.orderBy());
        boolean asc = isAscending(condition.direction());

        return switch (sortType) {
            case COMMENT_COUNT -> new OrderSpecifier[]{
                    asc ? article.commentCount.asc() : article.commentCount.desc(),
                    asc ? article.id.asc() : article.id.desc()
            };
            case VIEW_COUNT -> new OrderSpecifier[]{
                    asc ? article.viewCount.asc() : article.viewCount.desc(),
                    asc ? article.id.asc() : article.id.desc()
            };
            case PUBLISH_DATE -> new OrderSpecifier[]{
                    asc ? article.publishDate.asc() : article.publishDate.desc(),
                    asc ? article.id.asc() : article.id.desc()
            };
        };
    }

    private boolean isAscending(Sort.Direction direction) {
        return direction == Sort.Direction.ASC;
    }

}
