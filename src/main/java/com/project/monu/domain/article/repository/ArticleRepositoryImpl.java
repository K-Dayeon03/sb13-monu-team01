package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.QArticle;
import com.project.monu.domain.article.entity.QArticleInterest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
     * 현재 구현은 발행일 최신순 기준입니다.
     * 댓글 수/조회 수 정렬은 정렬 기준별 커서 조건을 추가하면 확장할 수 있습니다.
     */
    @Override
    public List<Article> searchByCursor(ArticleSearchCondition condition) {
        QArticle article = QArticle.article;

        return queryFactory
                .selectFrom(article)
                // ArticleDto.source 값을 만들 때 추가 조회가 발생하지 않도록 출처를 함께 조회합니다.
                .leftJoin(article.source).fetchJoin()
                // QueryDSL은 where에 null 조건이 들어오면 해당 조건을 무시합니다.
                // 그래서 각 조건 메서드는 값이 없을 때 null을 반환하도록 만들었습니다.
                .where(
                        keywordContains(condition.keyword()),
                        sourceEq(condition.source()),
                        interestEq(condition.interestId()),
                        publishDateGoe(condition.publishDateFrom()),
                        publishDateLoe(condition.publishDateTo()),
                        publishDateCursor(condition.nextAfter()),
                        notDeleted()
                )
                // 기본 정렬: 최신 기사순.
                // 같은 발행 시각의 기사가 여러 개일 수 있으므로 id를 보조 정렬로 사용합니다.
                .orderBy(article.publishDate.desc(), article.id.desc())
                .limit(condition.size() + 1)
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
                .where(
                        keywordContains(condition.keyword()),
                        sourceEq(condition.source()),
                        interestEq(condition.interestId()),
                        publishDateGoe(condition.publishDateFrom()),
                        publishDateLoe(condition.publishDateTo()),
                        notDeleted()
                )
                .fetchOne();

        return count == null ? 0L : count;
    }

    // 검색어가 있으면 제목 또는 요약 중 하나라도 부분 일치하는 기사를 조회합니다.
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QArticle.article.title.containsIgnoreCase(keyword)
                .or(QArticle.article.summary.containsIgnoreCase(keyword));
    }

    // 출처 이름이 있으면 해당 출처의 기사만 조회합니다.
    // Article.source는 ArticleSource 엔티티이므로 source.name으로 비교합니다.
    private BooleanExpression sourceEq(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }

        return QArticle.article.source.name.eq(source);
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
                        articleInterest.interestId.eq(interestId)
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
    private BooleanExpression publishDateCursor(Instant nextAfter) {
        if (nextAfter == null) {
            return null;
        }

        return QArticle.article.publishDate.lt(nextAfter);
    }

    // 논리 삭제되지 않은 기사만 조회합니다.
    private BooleanExpression notDeleted() {
        return QArticle.article.deletedAt.isNull();
    }
}
