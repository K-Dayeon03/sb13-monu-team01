package com.project.monu.domain.article.controller;

import com.project.monu.domain.article.dto.ArticleDto;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.service.ArticleService;
import com.project.monu.global.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 기사 목록을 조회합니다.
     *
     * keyword는 제목/요약 부분 검색에 사용하고,
     * interestId, source, publishDateFrom, publishDateTo는 필터 조건으로 사용합니다.
     * nextAfter, nextCursor는 커서 페이지네이션에서 다음 페이지 기준값으로 사용합니다.
     */
    @GetMapping
    public CursorPageResponse<ArticleDto> getArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID interestId,
            @RequestParam(required = false) String source,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant publishDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant publishDateTo,
            @RequestParam(defaultValue = "PUBLISH_DATE") ArticleSortType sortType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant nextAfter,
            @RequestParam(required = false) String nextCursor,
            @RequestParam(defaultValue = "10") int size,

            // 인증 기능이 붙기 전까지 viewedByMe 계산 확인용으로 임시 사용합니다.
            // 나중에는 SecurityContext에서 현재 로그인 사용자 ID를 꺼내도록 교체하면 됩니다.
            @RequestParam(required = false) UUID userId
    ) {
        ArticleSearchCondition condition = new ArticleSearchCondition(
                keyword,
                interestId,
                source,
                publishDateFrom,
                publishDateTo,
                sortType,
                nextAfter,
                nextCursor,
                size
        );

        return articleService.getArticles(condition, userId);
    }
}