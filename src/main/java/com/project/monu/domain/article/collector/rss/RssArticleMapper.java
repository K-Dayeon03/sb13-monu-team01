package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.entity.Article;
import com.rometools.rome.feed.synd.SyndEntry;
import org.springframework.stereotype.Component;

@Component
public class RssArticleMapper {

    public CollectedArticle toCollectedArticle(SyndEntry entry) {
        String summary = entry.getDescription() != null
                ? entry.getDescription().getValue()
                : null;

        return new CollectedArticle(
                entry.getTitle(),
                entry.getLink(),
                summary,
                entry.getPublishedDate().toInstant()
        );
    }
}
