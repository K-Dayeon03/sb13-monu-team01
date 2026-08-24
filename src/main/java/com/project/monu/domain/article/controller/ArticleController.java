package com.project.monu.domain.article.controller;

import com.project.monu.domain.article.dto.ArticleDto;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.service.ArticleService;
import com.project.monu.global.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

            // 나중에는 SecurityContext에서 현재 로그인 사용자 ID를 꺼내도록 교체하면 됩니다.
            // 인증 연동 전까지 클라이언트가 전달한 요청 사용자 ID로 viewedByMe를 계산합니다.
            @RequestHeader("MoNew-Request-User-ID") UUID userId
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

    @DeleteMapping("/{articleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable UUID articleId,
                           // API 요청 사용자 식별 규약에 따라 필수 헤더를 받습니다.
                           @RequestHeader("MoNew-Request-User-ID") UUID userId) {
        articleService.softDelete(articleId);
    }
}
