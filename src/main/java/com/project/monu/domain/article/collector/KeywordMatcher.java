package com.project.monu.domain.article.collector;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.naver.dto.InterestKeywords;

import java.util.List;
import java.util.UUID;

public class KeywordMatcher {

    public boolean containsKeyword(CollectedArticle article, String keyword) {
        return article.title().contains(keyword) || article.summary().contains(keyword);
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
