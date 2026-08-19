package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.entity.Article;

import java.util.List;

public interface ArticleRepositoryCustom {

    /**
     * 검색 조건과 커서 조건을 적용해 기사 목록을 조회합니다.
     * 구현체에서는 다음 페이지 여부 판단을 위해 size + 1개를 조회합니다.
     */
    List<Article> searchByCursor(ArticleSearchCondition condition);

    /**
     * 검색/필터 조건에 해당하는 전체 기사 수를 조회합니다.
     * 커서 조건은 전체 개수 계산에서 제외합니다.
     */
    long countByCondition(ArticleSearchCondition condition);
}
