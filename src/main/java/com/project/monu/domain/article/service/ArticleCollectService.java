package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.KeywordMatcher;
import com.project.monu.domain.article.collector.RetrySleeper;
import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.exception.ArticleCollectException;
import com.project.monu.domain.article.collector.exception.NaverApiException;
import com.project.monu.domain.article.collector.exception.NaverNetworkException;
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
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.notification.event.InterestArticleCreatedEvent;
import jakarta.persistence.EntityManager;
import com.project.monu.domain.interest.entity.Interest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
    private final ApplicationEventPublisher eventPublisher;
    private final SubscriptionRepository subscriptionRepository;
    private final RetrySleeper retrySleeper;

    private static final int NAVER_MAX_ATTEMPTS = 3;
    private static final long NAVER_RETRY_BACKOFF_MILLIS = 1000L;

    /**
     * 전체 API, RSS 출처를 순회하며 기사를 수집·저장한다.
     * @return 저장된 기사 수
     */
    public int collectAll() {
        // 관심사 + 키워드는 한 번만 로드 (매 기사마다 조회 방지)
        List<InterestKeywords> interestKeywords = loadInterestKeywords();
        int savedCount = 0;

        Map<UUID, Integer> articleCountByInterest = new HashMap<>();
        Map<UUID, String> interestNameById =  new HashMap<>();

        List<ArticleSource> sources = articleSourceRepository.findAll();

        for (ArticleSource source : sources) {
            if (!source.isEnabled()) {
                continue;
            }

            try {
                if (source.getType() == SourceType.RSS) {
                    // ==== RSS 수집 ====
                    List<CollectedArticle> articles = rssCollector.collect(source.getSourceUrl());
                    savedCount += processArticles(source, articles, interestKeywords, articleCountByInterest, interestNameById);
                }else if (source.getType() == SourceType.API){
                    // ==== 네이버 API 수집: 관심사 키워드마다 검색 ====
                    for (InterestKeywords ik : interestKeywords) {
                        for (String keyword : ik.keywords()) {
                            try {
                                List<CollectedArticle> articles = collectNaverWithRetry(keyword);
                                savedCount += processArticles(
                                        source,
                                        articles,
                                        interestKeywords,
                                        articleCountByInterest,
                                        interestNameById
                                );
                            } catch (NaverApiException e) {
                                log.warn("네이버 키워드 기사 수집 실패. source={}, keyword={}, status={}, errorCode={}, retryable={}",
                                        source.getName(),
                                        keyword,
                                        e.getStatusCode(),
                                        e.getErrorCode(),
                                        e.isRetryable(),
                                        e
                                );
                            } catch (ArticleCollectException e) {
                                log.warn("네이버 키워드 기사 수집 실패. source={}, keyword={}, type={}",
                                        source.getName(),
                                        keyword,
                                        e.getClass().getSimpleName(),
                                        e
                                );
                            }

                        }
                    }
                }
            } catch (NaverApiException e){
                log.warn("네이버 기사 수집 실패. source={}, status={}, errorCode={}, retryable={}",
                        source.getName(),
                        e.getStatusCode(),
                        e.getErrorCode(),
                        e.isRetryable(),
                        e
                        );
            } catch (ArticleCollectException e) {
                log.warn("기사 수집 실패. source={}, type={}",
                        source.getName(),
                        source.getType(),
                        e
                        );
            } catch (Exception e) {
                log.error("예상하지 못한 기사 수집 오류. source={}, type={}",
                        source.getName(),
                        source.getType(),
                        e
                );
                throw e;
            }
        }
        publishInterestArticleCreatedEvents(articleCountByInterest, interestNameById);

        log.info(">>> 수집 완료: {}건 저장", savedCount);
        return savedCount;
    }

    private List<CollectedArticle> collectNaverWithRetry(String keyword) {
        int attempt = 0;

        while (true) {
            attempt++;

            try {
                return naverCollector.collect(keyword);

            } catch (NaverApiException e) {
                if (!e.isRetryable() || attempt >= NAVER_MAX_ATTEMPTS) {
                    throw e;
                }

                log.warn(
                        "네이버 API 오류로 기사 수집 재시도. keyword={}, attempt={}, maxAttempts={}, status={}, errorCode={}",
                        keyword,
                        attempt,
                        NAVER_MAX_ATTEMPTS,
                        e.getStatusCode(),
                        e.getErrorCode()
                );
                retrySleeper.sleep(NAVER_RETRY_BACKOFF_MILLIS);

            } catch (NaverNetworkException e) {
                if (attempt >= NAVER_MAX_ATTEMPTS) {
                    throw e;
                }

                log.warn(
                        "네이버 네트워크 오류로 기사 수집 재시도. keyword={}, attempt={}, maxAttempts={}",
                        keyword,
                        attempt,
                        NAVER_MAX_ATTEMPTS,
                        e
                );
                retrySleeper.sleep(NAVER_RETRY_BACKOFF_MILLIS);
            }
        }
    }



    private int processArticles(ArticleSource source,
                                List<CollectedArticle> articles,
                                List<InterestKeywords> interestKeywords,
                                Map<UUID, Integer> articleCountByInterest,
                                Map<UUID, String> interestNameBtId) {
        int saved = 0;
        for (CollectedArticle article : articles) {
            List<UUID> matchedIds =
                    keywordMatcher.findMatchedInterests(article, interestKeywords);
            if (matchedIds.isEmpty()) {
                continue;
            }
            if (saveWithSource(source, article, matchedIds, articleCountByInterest, interestNameBtId)) {
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
                                   List<UUID> matchedInterestIds,
                                   Map<UUID, Integer> articleCountByInterest,
                                   Map<UUID, String> interestNameById) {

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

            articleCountByInterest.merge(interestId, 1, Integer::sum);

            interestNameById.putIfAbsent(interestId, interest.getName());
        }
        return true;
    }

    private void publishInterestArticleCreatedEvents(
            Map<UUID, Integer> articleCountByInterest,
            Map<UUID, String> interestNameById
    ) {
        articleCountByInterest.forEach((interestId, articleCount) -> {
            List<UUID> subscriberUserIds =
                    subscriptionRepository.findUserIdsByInterestId(interestId);

            eventPublisher.publishEvent(
                    new InterestArticleCreatedEvent(
                            interestId,
                            interestNameById.get(interestId),
                            articleCount,
                            subscriberUserIds
                    )
            );
        });
    }
}
