package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RssCollectorTest {

    @Mock
    private RssClient rssClient;
    @Mock
    private RssArticleMapper mapper;

    @InjectMocks
    private RssCollector rssCollector;

    @Test
    @DisplayName("RSS 피드를 수집해 CollecteArticle 목록으로 변환한다")
    void RSS_피드를_수집한다() {
        // given
        String rssUrl = "https://www.example.com/feed";
        SyndFeed feed = Mockito.mock(SyndFeed.class);
        SyndEntry entry1 = Mockito.mock(SyndEntry.class);
        SyndEntry entry2 = Mockito.mock(SyndEntry.class);

        when(rssClient.fetch(rssUrl)).thenReturn(feed);
        when(feed.getEntries()).thenReturn(List.of(entry1, entry2));

        CollectedArticle article1 = new CollectedArticle("제목1", "link1","요약1", Instant.now());
        CollectedArticle article2 = new CollectedArticle("제목2", "link2", "요약2", Instant.now());
        when(mapper.toCollectedArticle(entry1)).thenReturn(article1);
        when(mapper.toCollectedArticle(entry2)).thenReturn(article2);

        // when
        List<CollectedArticle> result = rssCollector.collect(rssUrl);

        // then
        assertThat(result).containsExactly(article1, article2);
    }

}
