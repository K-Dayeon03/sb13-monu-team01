package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NaverArticleMapperTest {

    @Test
    @DisplayName("Naver Item을 CollectedArticle로 변환한다.")
    void Naver_item을__CollectedArticle로_변환한다() {
        //given
        NaverArticleMapper mapper = new NaverArticleMapper();
        NaverNewsResponse.Item item = new NaverNewsResponse.Item(
                "삼성전자 신제품 출시", // title
                "https://example.com/news/1", // originalLink
                "https://n.news.naver.com/1", // link
                "삼성전자가 신제품을 출시했다.", // description
                "Thu, 13 Aug 2026 19:18:00 +0900" // pubDate
        );

        // when
        CollectedArticle result = mapper.toCollectedArticle(item);

        // then
        assertThat(result.title()).isEqualTo("삼성전자 신제품 출시");
        assertThat(result.originalLink()).isEqualTo("https://example.com/news/1");
        assertThat(result.summary()).isEqualTo("삼성전자가 신제품을 출시했다.");
    }
    
    @Test
    @DisplayName("Naver의 pubDate를 Instant로 변환한다.")
    void pubDate를_Instant로_변환한다() {
        // given
        NaverArticleMapper mapper = new NaverArticleMapper();
        NaverNewsResponse.Item item = new NaverNewsResponse.Item(
                "제목",
                "https://example.com/news/1",
                "https://n.news.naver.com/1",
                "요약",
                "Thu, 13 Aug 2026 19:18:00 +0900" // 한국시간 19:18 (+0900)
        );
    
        // when
        CollectedArticle result = mapper.toCollectedArticle(item);

        // then
        assertThat(result.publishedAt()).isEqualTo(Instant.parse("2026-08-13T10:18:00Z")); // UTC 10:18
        
    }

    @Test
    @DisplayName("pubDate 포맷이 잘못되면 예외를 던진다.")
    void pubDate_포맷이_잘못되면_예외를_던진다() {
        // given
        NaverArticleMapper mapper = new NaverArticleMapper();
        NaverNewsResponse.Item item = new NaverNewsResponse.Item(
                "제목",
                "https://example.com/news/1",
                "https://n.news.naver.com/1",
                "요약",
                "이상한날짜포맷"
        );

        // when && then
        assertThatThrownBy(() -> mapper.toCollectedArticle(item))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("제목의 b 태그가 제거된다.")
    void 제목의_b태그가_제거된다() {
        // given
        NaverArticleMapper mapper = new NaverArticleMapper();
        NaverNewsResponse.Item item = new NaverNewsResponse.Item(
                "삼성전자 <b>반도체</b> 신제품",   // ← b 태그 낀 제목
                "https://example.com/news/1",
                "https://n.news.naver.com/1",
                "요약",
                "Thu, 13 Aug 2026 19:18:00 +0900"
        );

        // when
        CollectedArticle result = mapper.toCollectedArticle(item);

        // then
        assertThat(result.title()).isEqualTo("삼성전자 반도체 신제품");
    }

    @Test
    @DisplayName("제목의 HTML 엔티티가 디코딩된다")
    void 제목의_HTML_엔티티가_디코딩된다() {
        // given
        NaverArticleMapper mapper = new NaverArticleMapper();
        NaverNewsResponse.Item item = new NaverNewsResponse.Item(
                "&quot;한 바구니에 담지 마라&quot;",   // ← 엔티티 낀 제목
                "https://example.com/news/1",
                "https://n.news.naver.com/1",
                "요약",
                "Thu, 13 Aug 2026 19:18:00 +0900"
        );

        // when
        CollectedArticle result = mapper.toCollectedArticle(item);

        // then
        assertThat(result.title()).isEqualTo("\"한 바구니에 담지 마라\"");
    }

}
