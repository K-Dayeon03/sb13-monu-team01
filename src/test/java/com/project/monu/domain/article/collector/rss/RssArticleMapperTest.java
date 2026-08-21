package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RssArticleMapperTest {

    private final RssArticleMapper mapper = new RssArticleMapper();

    @Test
    @DisplayName("SyndEntry를 CollectedArticle로 변환한다")
    void SyndEntry를_CollectedArticle로_변환한다() {

        // given
        SyndEntry entry = mock(SyndEntry.class);
        SyndContent description = mock(SyndContent.class);

        Instant publishedAt = Instant.parse("2026-08-19T06:00:00Z");

        when(entry.getTitle()).thenReturn("연합뉴스 기사 제목");
        when(entry.getLink()).thenReturn("https://www.yonhapnewstv.co.kr/news/123");
        when(entry.getDescription()).thenReturn(description);
        when(description.getValue()).thenReturn("기사 요약입니다");
        when(entry.getPublishedDate()).thenReturn(Date.from(publishedAt));

        // when
        CollectedArticle result = mapper.toCollectedArticle(entry);

        // then
        assertThat(result.title()).isEqualTo("연합뉴스 기사 제목");
        assertThat(result.originalLink()).isEqualTo("https://www.yonhapnewstv.co.kr/news/123");
        assertThat(result.summary()).isEqualTo("기사 요약입니다");
        assertThat(result.publishedAt()).isEqualTo(publishedAt);

    }

    @Test
    @DisplayName("요약(description)이 없어도 변환된다")
    void 요약이_없어도_변환된다() {
        // given
        SyndEntry entry = mock(SyndEntry.class);
        Instant publishedAt = Instant.parse("2026-08-19T06:00:00Z");

        when(entry.getTitle()).thenReturn("제목");
        when(entry.getLink()).thenReturn("https://example.com/1");
        when(entry.getDescription()).thenReturn(null);   // 요약 없음
        when(entry.getPublishedDate()).thenReturn(Date.from(publishedAt));

        // when
        CollectedArticle result = mapper.toCollectedArticle(entry);

        // then
        assertThat(result.summary()).isNull(); // null로 처리
        assertThat(result.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("제목과 요약의 HTML 태그 엔티티가 정리된다")
    void HTML이_정리된다() {
        // given
        SyndEntry entry = mock(SyndEntry.class);
        SyndContent description = mock(SyndContent.class);
        Instant publishedAt = Instant.parse("2026-08-19T06:00:00Z");

        when(entry.getTitle()).thenReturn("삼성 <b>반도체</b> &quot;신기록&quot;");
        when(entry.getLink()).thenReturn("https://example.com/1");
        when(entry.getDescription()).thenReturn(description);
        when(description.getValue()).thenReturn("&#60;출연&#62; 내용&nbsp;입니다");
        when(entry.getPublishedDate()).thenReturn(Date.from(publishedAt));

        // when
        CollectedArticle result = mapper.toCollectedArticle(entry);

        // then
        assertThat(result.title()).isEqualTo("삼성 반도체 \"신기록\"");
        assertThat(result.summary()).isEqualTo("<출연> 내용\u00A0입니다");
    }
    
    @Test
    @DisplayName("발행일이 없으면 현재 시각으로 대체한다")
    void 발행일이_없으면_현재시각() {

        SyndEntry entry = mock(SyndEntry.class);
        when(entry.getTitle()).thenReturn("제목");
        when(entry.getLink()).thenReturn("https://example.com/1");
        when(entry.getDescription()).thenReturn(null);
        when(entry.getPublishedDate()).thenReturn(null);  // 발행일 없음

        CollectedArticle result = mapper.toCollectedArticle(entry);

        assertThat(result.publishedAt()).isNotNull();  // 현재 시각으로 채워짐
        
    }
}
