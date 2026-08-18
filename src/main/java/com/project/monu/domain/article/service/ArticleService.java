package com.project.monu.domain.article.service;

import com.project.monu.domain.article.dto.ArticleDto;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.global.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;

    public CursorPageResponse<ArticleDto> getArticles(
            ArticleSearchCondition condition,
            UUID userId
    ) {
        // size가 0 이하로 들어오면 기본 페이지 크기 10을 사용합니다.
        // 이후 Repository에서는 size + 1개를 조회해서 다음 페이지 존재 여부를 판단합니다.
        int size = condition.size() <= 0 ? 10 : condition.size();

        // 검색어, 출처, 관심사, 날짜 범위, 커서 조건을 적용해 기사 목록을 조회합니다.
        // 실제 동적 쿼리 조립은 ArticleRepositoryImpl에서 QueryDSL로 처리합니다.
        List<Article> articles = articleRepository.searchByCursor(condition);

        // 요청한 size보다 1개 더 조회되었다면 다음 페이지가 있다는 뜻입니다.
        boolean hasNext = articles.size() > size;

        if (hasNext) {
            // 응답에는 요청한 size만큼만 내려주고, 추가로 조회한 1개는 hasNext 판단에만 사용합니다.
            articles = articles.subList(0, size);
        }

        // 현재 페이지에 포함된 기사 ID만 추립니다.
        // 이 ID 목록으로 사용자가 조회한 기사인지 한 번에 확인합니다.
        List<UUID> articleIds = articles.stream()
                .map(Article::getId)
                .toList();

        // viewedByMe는 Article 자체의 컬럼이 아니라 "현재 사용자 기준" 계산값입니다.
        // 목록의 각 기사마다 조회 이력을 따로 조회하면 N+1 문제가 생기므로,
        // 현재 페이지의 기사 ID들을 기준으로 조회 이력을 한 번에 가져옵니다.
        Set<UUID> viewedArticleIds = articleIds.isEmpty()|| userId == null
                ? Set.of()
                : articleViewRepository.findViewedArticleIds(userId, articleIds);

        // Entity는 DB 구조를 표현하고, DTO는 API 응답 형태를 표현합니다.
        // ArticleSource 엔티티에서는 화면/응답에 필요한 출처 이름만 꺼내 담습니다.
        List<ArticleDto> content = articles.stream()
                .map(article -> new ArticleDto(
                        article.getId(),
                        article.getSource().getName(),
                        article.getSourceUrl(),
                        article.getTitle(),
                        article.getPublishDate(),
                        article.getSummary(),
                        article.getCommentCount(),
                        article.getViewCount(),
                        viewedArticleIds.contains(article.getId())
                ))
                .toList();

        // 현재 페이지의 마지막 기사를 기준으로 다음 페이지 커서를 만듭니다.
        // 현재 구현은 publishDate 기준 커서 페이지네이션이므로 nextAfter에 마지막 발행 시각을 담습니다.
        Article lastArticle = articles.isEmpty() ? null : articles.get(articles.size() - 1);

        return new CursorPageResponse<>(
                content,
                hasNext && lastArticle != null ? lastArticle.getId().toString() : null,
                hasNext && lastArticle != null ? lastArticle.getPublishDate() : null,
                size,
                articleRepository.countByCondition(condition),
                hasNext
        );
    }
}
