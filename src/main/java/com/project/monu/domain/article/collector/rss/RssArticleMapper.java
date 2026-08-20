package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.Instant;

@Component
public class RssArticleMapper {

    public CollectedArticle toCollectedArticle(SyndEntry entry) {
        String summary = entry.getDescription() != null
                ? cleanText(entry.getDescription().getValue())
                : null;
        Instant publishedAt = entry.getPublishedDate() != null
                ? entry.getPublishedDate().toInstant()
                : Instant.now();

        return new CollectedArticle(
                cleanText(entry.getTitle()),
                entry.getLink(),
                summary,
                publishedAt
        );
    }

    private String cleanText(String text) {
        if (text == null) {
            return null;
        }
        String withoutTags = text.replaceAll("<[^>]*>", "");
        return HtmlUtils.htmlUnescape(withoutTags);
    }
}
