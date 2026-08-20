package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RssArticleMapper {

    public CollectedArticle toCollectedArticle(SyndEntry entry) {
        String summary = entry.getDescription() != null
                ? entry.getDescription().getValue()
                : null;
        Instant publishedAt = entry.getPublishedDate() != null
                ? entry.getPublishedDate().toInstant()
                : Instant.now();

        return new CollectedArticle(
                entry.getTitle(),
                entry.getLink(),
                summary,
                publishedAt
        );
    }
}
