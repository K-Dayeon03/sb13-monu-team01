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
}
