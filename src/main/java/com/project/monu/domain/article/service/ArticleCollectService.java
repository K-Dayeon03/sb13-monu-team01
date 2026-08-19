package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.KeywordMatcher;
import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleInterest;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
import jakarta.persistence.EntityManager;
import com.project.monu.domain.interest.entity.Interest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class ArticleCollectService {

    private final ArticleRepository articleRepository;
    private final ArticleSourceRepository articleSourceRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final KeywordMatcher keywordMatcher;
    private final EntityManager entityManager;

    public void save(CollectedArticle article, List<UUID> matchedInterestIds) {
        // 중복이면 저장하지 않음
        if (articleRepository.existsBySourceUrl(article.originalLink())) {
            return;
        }

        // 2. 출처 조회
        ArticleSource source = articleSourceRepository.findByName("NAVER")
                .orElseThrow(()-> new IllegalArgumentException("출처를 찾을 수 없습니다: NAVER"));

        // Article 생성
        Article newArticle = Article.builder()
                .source(source)
                .sourceUrl(article.originalLink())
                .title(article.title())
                .publishDate(article.publishedAt())
                .summary(article.summary())
                .build();

        // 저장
        articleRepository.save(newArticle);

        // 관심사 매칭 기록
        for (UUID interestId : matchedInterestIds) {
            Interest interest = entityManager.getReference(Interest.class, interestId);
            ArticleInterest articleInterest = ArticleInterest.builder()
                    .article(newArticle)
                    .interest(interest)
                    .build();
            articleInterestRepository.save(articleInterest);
        }



    }

}
