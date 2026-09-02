package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleInterest;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.interest.entity.Interest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArticleCollectPersistenceService {

    private final ArticleRepository  articleRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final EntityManager entityManager;

    /**
     * 기사 한 건과 관심사 연결을 하나의 짧은 트랜잭션으로 저장합니다.
     */

    @Transactional
    public SaveResult saveIfAbsent (
            ArticleSource source,
            CollectedArticle collectedArticle,
            List<UUID> matchedInterestIds
    ){
        if (articleRepository.existsBySourceUrl(collectedArticle.originalLink())) {
            return SaveResult.duplicate();
        }

        Article article = Article.builder()
                .source(source)
                .sourceUrl(collectedArticle.originalLink())
                .title(collectedArticle.title())
                .publishDate(collectedArticle.publishedAt())
                .summary(collectedArticle.summary())
                .build();

        articleRepository.save(article);

        Map<UUID, String> interestNames = new LinkedHashMap<>();

        for (UUID interestId : matchedInterestIds) {
            Interest interest =
                    entityManager.getReference(Interest.class, interestId);

            ArticleInterest articleInterest = ArticleInterest.builder()
                    .article(article)
                    .interest(interest)
                    .build();

            articleInterestRepository.save(articleInterest);
            interestNames.put(interestId, interest.getName());
        }
        return SaveResult.saved(interestNames);
    }

    public record SaveResult(
            boolean saved,
            Map<UUID, String> interestNames
    ) {
        public static SaveResult saved(Map<UUID, String> interestNames) {
            return new SaveResult(true, Map.copyOf(interestNames));
        }

        public static SaveResult duplicate() {
            return new SaveResult(false, Map.of());
        }
    }
}
