package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.KeywordMatcher;
import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.naver.NaverCollector;
import com.project.monu.domain.article.collector.naver.dto.InterestKeywords;
import com.project.monu.domain.article.collector.rss.RssCollector;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleInterest;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.repository.KeywordRepository;
import jakarta.persistence.EntityManager;
import com.project.monu.domain.interest.entity.Interest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ArticleCollectService {

    private final ArticleRepository articleRepository;
    private final ArticleSourceRepository articleSourceRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final KeywordRepository keywordRepository;
    private final KeywordMatcher keywordMatcher;
    private final NaverCollector naverCollector;
    private final RssCollector rssCollector;
    private final EntityManager entityManager;

    /**
     * 전체 API, RSS 출처를 순회하며 기사를 수집·저장한다.
     * @return 저장된 기사 수
     */
    public int collectAll() {
        // 관심사 + 키워드는 한 번만 로드 (매 기사마다 조회 방지)
        List<InterestKeywords> interestKeywords = loadInterestKeywords();
        int savedCount = 0;

        List<ArticleSource> sources = articleSourceRepository.findAll();

        for (ArticleSource source : sources) {
            if (!source.isEnabled()) {
                continue;
            }

            try {
                if (source.getType() == SourceType.RSS) {
                    // ==== RSS 수집 ====
                    List<CollectedArticle> articles = rssCollector.collect(source.getSourceUrl());
                    savedCount += processArticles(source, articles, interestKeywords);
                }else if (source.getType() == SourceType.API){
                    // ==== 네이버 API 수집: 관심사 키워드마다 검색 ====
                    for (InterestKeywords ik : interestKeywords) {
                        for (String keyword : ik.keywords()) {
                            List<CollectedArticle> articles = naverCollector.collect(keyword);
                            savedCount += processArticles(source, articles, interestKeywords);
                        }
                    }
                }
            }catch (Exception e){
                log.warn("출처 수집 실패: {} - {}", source.getName(), e.getMessage());
            }
        }
        log.info(">>> 수집 완료: {}건 저장", savedCount);
        return savedCount;
    }

    private int processArticles(ArticleSource source,
                                List<CollectedArticle> articles,
                                List<InterestKeywords> interestKeywords) {
        int saved = 0;
        for (CollectedArticle article : articles) {
            List<UUID> matchedIds =
                    keywordMatcher.findMatchedInterests(article, interestKeywords);
            if (matchedIds.isEmpty()) {
                continue;
            }
            if (saveWithSource(source, article, matchedIds)) {
                saved++;
            }
        }
        return saved;
    }

    /**
     * DB의 모든 키워드를 관심사별로 묶어 InterestKeywords 리스트로 변환
     */
    private List<InterestKeywords> loadInterestKeywords() {
        Map<UUID, List<String>> grouped = keywordRepository.findAllWithInterest().stream()
                .collect(Collectors.groupingBy(
                        k -> k.getInterest().getId(),
                        Collectors.mapping(Keyword::getKeyword, Collectors.toList())
                ));
        return grouped.entrySet().stream()
                .map(e -> new InterestKeywords(e.getKey(), e.getValue()))
                .toList();
    }

    /**
     * 넘겨받은 출처로 기사 1건을 저장. 중복이면 저장하지 않고 false 반환.
     */
    private boolean saveWithSource(ArticleSource source,
                                   CollectedArticle article,
                                   List<UUID> matchedInterestIds) {
        if (articleRepository.existsBySourceUrl(article.originalLink())) {
            return false;
        }

        Article newArticle = Article.builder()
                .source(source)
                .sourceUrl(article.originalLink())
                .title(article.title())
                .publishDate(article.publishedAt())
                .summary(article.summary())
                .build();
        articleRepository.save(newArticle);

        for (UUID interestId : matchedInterestIds) {
            Interest interest = entityManager.getReference(Interest.class, interestId);
            ArticleInterest articleInterest = ArticleInterest.builder()
                    .article(newArticle)
                    .interest(interest)
                    .build();
            articleInterestRepository.save(articleInterest);
        }
        return true;
    }
}
