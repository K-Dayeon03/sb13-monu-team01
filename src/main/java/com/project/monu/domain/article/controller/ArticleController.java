package com.project.monu.domain.article.controller;

import com.project.monu.domain.article.dto.response.ArticleDto;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.dto.response.ArticleRestoreResultDto;
import com.project.monu.domain.article.service.ArticleBackupService;
import com.project.monu.domain.article.service.ArticleService;
import com.project.monu.global.constant.RequestHeaders;
import com.project.monu.global.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleBackupService articleBackupService;

    /**
     * 기사 목록을 조회합니다.
     *
     * keyword는 제목/요약 부분 검색에 사용하고,
     * interestId, sourceIn, publishDateFrom, publishDateTo는 필터 조건으로 사용합니다.
     * after, cursor는 커서 페이지네이션에서 다음 페이지 기준값으로 사용합니다.
     */
    @GetMapping
    public CursorPageResponse<ArticleDto> getArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID interestId,
            @RequestParam(required = false) List<String> sourceIn,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant publishDateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant publishDateTo,
            @RequestParam(defaultValue = "publishDate") String orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant after,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int limit,

            // 나중에는 SecurityContext에서 현재 로그인 사용자 ID를 꺼내도록 교체하면 됩니다.
            // 인증 연동 전까지 클라이언트가 전달한 요청 사용자 ID로 viewedByMe를 계산합니다.
            @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId
    ) {
        ArticleSearchCondition condition = new ArticleSearchCondition(
                keyword,
                interestId,
                normalizeSourceIn(sourceIn),
                publishDateFrom,
                publishDateTo,
                parseOrderBy(orderBy),
                direction,
                after,
                cursor,
                limit
        );

        return articleService.getArticles(condition, userId);
    }

    private ArticleSortType parseOrderBy(String orderBy) {
        try {
            return ArticleSortType.from(orderBy);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private List<String> normalizeSourceIn(List<String> sourceIn) {
        if (sourceIn == null || sourceIn.isEmpty()) {
            return null;
        }

        return sourceIn.stream()
                .flatMap(source -> Arrays.stream(source.split(",")))
                .map(String::trim)
                .filter(source -> !source.isBlank())
                .toList();
    }

    @DeleteMapping("/{articleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDelete(@PathVariable UUID articleId,
                           // API 요청 사용자 식별 규약에 따라 필수 헤더를 받습니다.
                           @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId) {
        articleService.softDelete(articleId);
    }

    @DeleteMapping("/{articleId}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDelete(@PathVariable UUID articleId) {
        articleService.hardDelete(articleId);
    }

    @GetMapping("/restore")
    public List<ArticleRestoreResultDto> restoreArticles(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant to
    ) {
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from은 to보다 늦을 수 없습니다.");
        }

        return articleBackupService.restore(from, to);
    }

    @GetMapping("/sources")
    public List<String> getSources() {
        return articleService.getSources();
    }

    @GetMapping("/{articleId}")
    public ArticleDto getArticle(@PathVariable UUID articleId,
                                 @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId) {
        return articleService.getArticle(articleId, userId);
    }

}
