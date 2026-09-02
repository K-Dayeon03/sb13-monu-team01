package com.project.monu.domain.article.collector;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.naver.dto.InterestKeywords;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class KeywordMatcher {

    public boolean containsKeyword(CollectedArticle article, String keyword) {
        String title = article.title() != null ? article.title() : "";
        String summary = article.summary() != null ? article.summary() : "";
        return title.contains(keyword) || summary.contains(keyword);
    }

    public boolean matchesInterest(CollectedArticle article, InterestKeywords interest) {
        return interest.keywords().stream()
                .anyMatch(keyword -> containsKeyword(article, keyword));
    }

    public List<UUID> findMatchedInterests(CollectedArticle article,
                                           List<InterestKeywords> interests) {
        return interests.stream()
                .filter(interest -> matchesInterest(article, interest))
                .map(InterestKeywords::interestId)
                .toList();
    }
}
