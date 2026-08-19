package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RssCollector {


    private final RssClient rssClient;
    private final RssArticleMapper mapper;

    public List<CollectedArticle> collect(String rssUrl) {
        return rssClient.fetch(rssUrl)
                .getEntries().stream()
                .map(mapper::toCollectedArticle)
                .toList();
    }
}
