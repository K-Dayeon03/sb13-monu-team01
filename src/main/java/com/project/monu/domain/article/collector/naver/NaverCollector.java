package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NaverCollector {

    private final NaverNewsClient naverNewsClient;
    private final NaverArticleMapper naverArticleMapper;

    public NaverCollector(NaverNewsClient naverNewsClient,
                          NaverArticleMapper naverArticleMapper) {
        this.naverNewsClient = naverNewsClient;
        this.naverArticleMapper = naverArticleMapper;
    }

    public List<CollectedArticle> collect(String keyword) {
        NaverNewsResponse response = naverNewsClient.search(keyword);

        return response.items().stream()
                .map(naverArticleMapper::toCollectedArticle)
                .toList();
    }
}
