package com.project.monu.domain.article.controller;

import com.project.monu.domain.article.dto.ArticleDto;
import com.project.monu.domain.article.dto.CursorPageResponse;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private ArticleController articleController;

    @MockitoBean
    private ArticleService articleService;

    @Test
    void 기사_목록을_조회한다() throws Exception {
        // given
        // Controller만 단독으로 테스트하기 위해 MockMvc를 직접 구성합니다.
        // Service는 MockitoBean으로 대체해서 요청 파라미터 바인딩과 응답 직렬화만 검증합니다.
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();

        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant publishDate = Instant.parse("2026-08-18T00:00:00Z");
        Instant nextAfter = Instant.parse("2026-08-17T00:00:00Z");

        ArticleDto article = new ArticleDto(
                articleId,
                "NAVER",
                "https://example.com/articles/1",
                "AI 뉴스",
                publishDate,
                "AI 뉴스 요약",
                10L,
                100L,
                true
        );

        CursorPageResponse<ArticleDto> response = CursorPageResponse.of(
                List.of(article),
                "10_" + articleId,
                nextAfter,
                10,
                1L,
                false
        );

        when(articleService.getArticles(any(ArticleSearchCondition.class), eq(userId)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/articles")
                        .param("keyword", "AI")
                        .param("interestId", UUID.randomUUID().toString())
                        .param("source", "NAVER")
                        .param("publishDateFrom", "2026-08-01T00:00:00Z")
                        .param("publishDateTo", "2026-08-31T23:59:59Z")
                        .param("sortType", "COMMENT_COUNT")
                        .param("nextAfter", "2026-08-17T00:00:00Z")
                        .param("nextCursor", "10_" + articleId)
                        .param("size", "10")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(articleId.toString()))
                .andExpect(jsonPath("$.content[0].source").value("NAVER"))
                .andExpect(jsonPath("$.content[0].title").value("AI 뉴스"))
                .andExpect(jsonPath("$.content[0].viewedByMe").value(true))
                .andExpect(jsonPath("$.nextCursor").value("10_" + articleId))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    void 요청_파라미터를_검색조건으로_변환해_Service에_전달한다() throws Exception {
        // given
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();

        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(articleService.getArticles(any(ArticleSearchCondition.class), eq(userId)))
                .thenReturn(CursorPageResponse.of(List.of(), null, null, 20, 0L, false));

        // when
        mockMvc.perform(get("/api/articles")
                        .param("keyword", "경제")
                        .param("interestId", interestId.toString())
                        .param("source", "CHOSUN")
                        .param("publishDateFrom", "2026-08-01T00:00:00Z")
                        .param("publishDateTo", "2026-08-31T23:59:59Z")
                        .param("sortType", "VIEW_COUNT")
                        .param("nextAfter", "2026-08-10T00:00:00Z")
                        .param("nextCursor", "100_" + UUID.randomUUID())
                        .param("size", "20")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());

        // then
        // Controller가 받은 query parameter를 ArticleSearchCondition으로 잘 묶어 Service에 넘겼는지 확인합니다.
        ArgumentCaptor<ArticleSearchCondition> conditionCaptor = ArgumentCaptor.forClass(ArticleSearchCondition.class);
        verify(articleService).getArticles(conditionCaptor.capture(), eq(userId));

        ArticleSearchCondition condition = conditionCaptor.getValue();

        assertThat(condition.keyword()).isEqualTo("경제");
        assertThat(condition.interestId()).isEqualTo(interestId);
        assertThat(condition.source()).isEqualTo("CHOSUN");
        assertThat(condition.publishDateFrom()).isEqualTo(Instant.parse("2026-08-01T00:00:00Z"));
        assertThat(condition.publishDateTo()).isEqualTo(Instant.parse("2026-08-31T23:59:59Z"));
        assertThat(condition.sortType()).isEqualTo(ArticleSortType.VIEW_COUNT);
        assertThat(condition.nextAfter()).isEqualTo(Instant.parse("2026-08-10T00:00:00Z"));
        assertThat(condition.nextCursor()).startsWith("100_");
        assertThat(condition.size()).isEqualTo(20);
    }

    @Test
    void 선택_파라미터가_없으면_기본_정렬과_기본_size를_사용한다() throws Exception {
        // given
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(articleController).build();

        when(articleService.getArticles(any(ArticleSearchCondition.class), eq(null)))
                .thenReturn(CursorPageResponse.of(List.of(), null, null, 10, 0L, false));

        // when
        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk());

        // then
        // sortType과 size는 Controller의 @RequestParam defaultValue로 기본값이 적용됩니다.
        ArgumentCaptor<ArticleSearchCondition> conditionCaptor = ArgumentCaptor.forClass(ArticleSearchCondition.class);
        verify(articleService).getArticles(conditionCaptor.capture(), eq(null));

        ArticleSearchCondition condition = conditionCaptor.getValue();

        assertThat(condition.keyword()).isNull();
        assertThat(condition.interestId()).isNull();
        assertThat(condition.source()).isNull();
        assertThat(condition.publishDateFrom()).isNull();
        assertThat(condition.publishDateTo()).isNull();
        assertThat(condition.sortType()).isEqualTo(ArticleSortType.PUBLISH_DATE);
        assertThat(condition.nextAfter()).isNull();
        assertThat(condition.nextCursor()).isNull();
        assertThat(condition.size()).isEqualTo(10);
    }
}
