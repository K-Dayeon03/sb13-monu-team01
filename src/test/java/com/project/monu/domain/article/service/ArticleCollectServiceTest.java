package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.KeywordMatcher;
import com.project.monu.domain.article.collector.RetrySleeper;
import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.exception.ArticleCollectException;
import com.project.monu.domain.article.collector.exception.NaverApiException;
import com.project.monu.domain.article.collector.exception.NaverNetworkException;
import com.project.monu.domain.article.collector.exception.RssCollectException;
import com.project.monu.domain.article.collector.naver.NaverCollector;
import com.project.monu.domain.article.collector.rss.RssCollector;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.repository.KeywordRepository;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.notification.event.InterestArticleCreatedEvent;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleCollectServiceTest")
class ArticleCollectServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleSourceRepository articleSourceRepository;
    @Mock
    private ArticleInterestRepository articleInterestRepository;
    @Mock
    private KeywordRepository keywordRepository;
    @Mock
    private KeywordMatcher keywordMatcher;
    @Mock
    private NaverCollector naverCollector;
    @Mock
    private RssCollector rssCollector;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private RetrySleeper retrySleeper;
    @Mock
    private ArticleCollectPersistenceService persistenceService;

    @InjectMocks
    private ArticleCollectService articleCollectService;

    private ArticleSource rssSource(String name) {
        return ArticleSource.builder()
                .name(name)
                .type(SourceType.RSS)
                .sourceUrl("http://example.com/rss/" + name)
                .build();
    }

    private CollectedArticle article(String title, String link) {
        return new CollectedArticle(title, link, "요약", Instant.now());
    }


    private ArticleSource apiSource(String name) {
        return ArticleSource.builder()
                .name(name)
                .type(SourceType.API)
                .sourceUrl("https://openapi.naver.com/v1/search/news.json")
                .build();
    }

    private void givenArticleSaved(UUID interestId, String interestName) {
        given(persistenceService.saveIfAbsent(
                any(ArticleSource.class),
                any(CollectedArticle.class),
                anyList()
        )).willReturn(
                ArticleCollectPersistenceService.SaveResult.saved(
                        java.util.Map.of(interestId, interestName)
                )
        );
    }

    @Test
    @DisplayName("비활성화된 출처는 수집하지 않는다")
    void 비활성화된_출처는_수집하지_않는다() {
        // given
        ArticleSource source = rssSource("YEONHAP");
        source.disable();
        given(articleSourceRepository.findAll()).willReturn(List.of(source));
        given(keywordRepository.findAllWithInterest()).willReturn(List.of());

        // when
        int saved = articleCollectService.collectAll();

        // then
        assertThat(saved).isZero();
        verify(rssCollector, never()).collect(anyString());
    }

    @Test
    @DisplayName("매칭된 기사는 저장 전용 서비스에 위임한다")
    void 매칭된_기사는_저장_전용_서비스에_위임한다() {
        // given
        UUID interestId = UUID.randomUUID();

        CollectedArticle collectedArticle =
                article("제목", "https://example.com/1");

        ArticleSource source = rssSource("YEONHAP");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));
        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of());
        given(rssCollector.collect(anyString()))
                .willReturn(List.of(collectedArticle));
        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));

        given(persistenceService.saveIfAbsent(
                source,
                collectedArticle,
                List.of(interestId)
        )).willReturn(
                ArticleCollectPersistenceService.SaveResult.saved(
                        java.util.Map.of(interestId, "테스트 관심사")
                )
        );

        // when
        int saved = articleCollectService.collectAll();

        // then
        assertThat(saved).isEqualTo(1);

        verify(persistenceService).saveIfAbsent(
                source,
                collectedArticle,
                List.of(interestId)
        );
    }
    
    @Test
    @DisplayName("이미 존재하는 링크의 기사는 저장하지 않는다")
    void 중복된_기사는_저장하지_않는다() {
        // given
        ArticleSource source = rssSource("YEONHAP");
        given(articleSourceRepository.findAll()).willReturn(List.of(source));
        given(keywordRepository.findAllWithInterest()).willReturn(List.of());
        given(rssCollector.collect(anyString()))
                .willReturn(List.of(article("제목", "https://example.com/dup")));
        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(UUID.randomUUID()));
        given(persistenceService.saveIfAbsent(
                eq(source),
                any(CollectedArticle.class),
                anyList()
        )).willReturn(
                ArticleCollectPersistenceService.SaveResult.duplicate()
        );
    
        // when
        int saved = articleCollectService.collectAll();
    
        // then
        assertThat(saved).isZero();
        verify(persistenceService).saveIfAbsent(
                eq(source),
                any(CollectedArticle.class),
                anyList()
        );
        verify(eventPublisher, never()).publishEvent(any());
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("동시 저장으로 중복 제약이 발생하면 중복 기사로 처리한다")
    void 동시_저장으로_중복_제약이_발생하면_중복_기사로_처리한다() {
        // given
        ArticleSource source = rssSource("YEONHAP");
        UUID interestId = UUID.randomUUID();

        given(articleSourceRepository.findAll()).willReturn(List.of(source));
        given(keywordRepository.findAllWithInterest()).willReturn(List.of());
        given(rssCollector.collect(source.getSourceUrl()))
                .willReturn(List.of(article("제목", "https://example.com/race")));
        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));
        given(articleRepository.existsBySourceUrl("https://example.com/race"))
                .willReturn(true);
        given(persistenceService.saveIfAbsent(
                eq(source),
                any(CollectedArticle.class),
                eq(List.of(interestId))
        )).willThrow(new DataIntegrityViolationException("duplicate source_url"));

        // when
        int saved = articleCollectService.collectAll();

        // then
        assertThat(saved).isZero();
        verify(eventPublisher, never()).publishEvent(any());
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("매칭된 관심사가 없으면 저장하지 않는다")
    void 매칭된_관심사가_없으면_저장하지_않는다() {
        // given
        ArticleSource source = rssSource("YEONHAP");
        given(articleSourceRepository.findAll()).willReturn(List.of(source));
        given(keywordRepository.findAllWithInterest()).willReturn(List.of());
        given(rssCollector.collect(anyString()))
                .willReturn(List.of(article("제목", "https://example.com/2")));
        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of());
        // when
        int saved = articleCollectService.collectAll();

        // then
        assertThat(saved).isZero();
        verify(eventPublisher, never()).publishEvent(any());
        verifyNoInteractions(subscriptionRepository);
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("한 출처가 실패해도 나머지 출처는 계속 수집한다")
    void 한_출처가_실패해도_나머지는_계속_수집한다() {
        // given
        UUID interestId = UUID.randomUUID();

        givenArticleSaved(interestId, "테스트 관심사");

        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));


        ArticleSource failSource = rssSource("FAIL");
        ArticleSource okSource = rssSource("OK");
        given(articleSourceRepository.findAll()).willReturn(List.of(failSource, okSource));
        given(keywordRepository.findAllWithInterest()).willReturn(List.of());
        given(rssCollector.collect(failSource.getSourceUrl()))
                .willThrow(new RssCollectException("RSS 실패", new RuntimeException("연결 실패")));
        given(rssCollector.collect(okSource.getSourceUrl()))
                .willReturn(List.of(article("제목", "https://example.com/ok")));

        // when
        int saved = articleCollectService.collectAll();

        // then
        assertThat(saved).isEqualTo(1);
        verify(persistenceService, times(1))
                .saveIfAbsent(
                        eq(okSource),
                        any(CollectedArticle.class),
                        eq(List.of(interestId))
                );
    }

    @Test
    @DisplayName("예상하지 못한 시스템 오류는 다시 던진다.")
    void 예상하지_못한_시스템_오류는_다시_던진다() {
        // given
        ArticleSource source = rssSource("ERROR");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of());

        RuntimeException systemException =
                new RuntimeException("DB 또는 시스템 오류");

        given(rssCollector.collect(source.getSourceUrl()))
                .willThrow(systemException);

        // when & then
        assertThatThrownBy(articleCollectService::collectAll)
                .isSameAs(systemException);
    }

    @Test
    @DisplayName("신규 기사가 저장되면 관심사 기사 생성 이벤트를 발행한다")
    void 신규_기사가_저장되면_관심사_기사_생성_이벤트를_발행한다() {
        // given
        UUID interestId = UUID.randomUUID();
        UUID subscriberId1 = UUID.randomUUID();
        UUID subscriberId2 = UUID.randomUUID();

        given(subscriptionRepository.findUserIdsByInterestId(interestId))
                .willReturn(List.of(subscriberId1, subscriberId2));

        ArticleSource source = rssSource("YEONHAP");
        givenArticleSaved(interestId, "축구");


        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of());

        given(rssCollector.collect(source.getSourceUrl()))
                .willReturn(List.of(
                        article("축구 기사", "https://example.com/football")));

        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));
        // when
        int savedCount = articleCollectService.collectAll();

        // then
        // then
        assertThat(savedCount).isEqualTo(1);

        ArgumentCaptor<Object> eventCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        assertThat(eventCaptor.getValue())
                .isInstanceOf(InterestArticleCreatedEvent.class);

        InterestArticleCreatedEvent event =
                (InterestArticleCreatedEvent) eventCaptor.getValue();

        assertThat(event.interestId()).isEqualTo(interestId);
        assertThat(event.interestName()).isEqualTo("축구");
        assertThat(event.articleCount()).isEqualTo(1);
        assertThat(event.subscriberUserIds())
                .containsExactly(subscriberId1, subscriberId2);

    }

    @Test
    @DisplayName("같은 관심사의 신규 기사 여러 건은 하나의 이벤트로 집계한다.")
    void 같은_관심사의_신규_기사_여러_건은_하나의_이벤트로_집계한다() {
        UUID interestId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();

        ArticleSource source = rssSource("YEONHAP");
        givenArticleSaved(interestId, "축구");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of());

        given(rssCollector.collect(source.getSourceUrl()))
                .willReturn(List.of(
                        article("축구 기사 1", "https://example.com/football/1"),
                        article("축구 기사 2", "https://example.com/football/2")
                ));

        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));



        given(subscriptionRepository.findUserIdsByInterestId(interestId))
                .willReturn(List.of(subscriberId));

        // when
        int savedCount = articleCollectService.collectAll();

        // then
        assertThat(savedCount).isEqualTo(2);

        ArgumentCaptor<Object> eventCaptor =
                ArgumentCaptor.forClass(Object.class);

        verify(eventPublisher, times(1))
                .publishEvent(eventCaptor.capture());

        verify(persistenceService, times(2))
                .saveIfAbsent(
                        eq(source),
                        any(CollectedArticle.class),
                        eq(List.of(interestId))
                );

        InterestArticleCreatedEvent event =
                (InterestArticleCreatedEvent) eventCaptor.getValue();

        assertThat(event.interestId()).isEqualTo(interestId);
        assertThat(event.interestName()).isEqualTo("축구");
        assertThat(event.articleCount()).isEqualTo(2);
        assertThat(event.subscriberUserIds())
                .containsExactly(subscriberId);
    }

    @Test
    @DisplayName("재시도 가능한 네이버 오류가 발생하면 다시 호출하고 성공한 기사를 저장한다")
    void 재시도_가능한_네이버_오류가_발생하면_다시_호출한다() {
        // given
        UUID interestId = UUID.randomUUID();

        ArticleSource source = apiSource("NAVER");

        Interest interest = mock(Interest.class);
        Keyword keyword = mock(Keyword.class);

        given(interest.getId()).willReturn(interestId);
        givenArticleSaved(interestId, "축구");

        given(keyword.getInterest()).willReturn(interest);
        given(keyword.getKeyword()).willReturn("축구");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of(keyword));

        NaverApiException retryableException = new NaverApiException(
                429,
                "429",
                "호출 한도를 초과했습니다.",
                true,
                new RuntimeException("네이버 API 오류")
        );

        given(naverCollector.collect("축구"))
                .willThrow(retryableException)
                .willReturn(List.of(
                        article("축구 기사", "https://example.com/naver/1")
                ));

        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));


        given(subscriptionRepository.findUserIdsByInterestId(interestId))
                .willReturn(List.of());

        // when
        int savedCount = articleCollectService.collectAll();

        // then
        assertThat(savedCount).isEqualTo(1);

        verify(persistenceService).saveIfAbsent(
                eq(source),
                any(CollectedArticle.class),
                eq(List.of(interestId))
        );
        verify(naverCollector, times(2))
                .collect("축구");

        verify(retrySleeper)
                .sleep(1000L);
    }

    @Test
    @DisplayName("네이버 네트워크 오류가 발생하면 다시 호출하고 성공한 기사를 저장한다")
    void 네이버_네트워크_오류가_발생하면_다시_호출한다() {
        // given
        UUID interestId = UUID.randomUUID();

        ArticleSource source = apiSource("NAVER");

        Interest interest = mock(Interest.class);
        Keyword keyword = mock(Keyword.class);

        given(interest.getId()).willReturn(interestId);
        givenArticleSaved(interestId, "축구");

        given(keyword.getInterest()).willReturn(interest);
        given(keyword.getKeyword()).willReturn("축구");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of(keyword));

        NaverNetworkException networkException =
                new NaverNetworkException(
                        "네이버 뉴스 API 연결에 실패했습니다.",
                        new RuntimeException("connection timeout")
                );

        given(naverCollector.collect("축구"))
                .willThrow(networkException)
                .willReturn(List.of(
                        article("축구 기사", "https://example.com/naver/network")
                ));

        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));

        given(subscriptionRepository.findUserIdsByInterestId(interestId))
                .willReturn(List.of());

        // when
        int savedCount = articleCollectService.collectAll();

        // then
        assertThat(savedCount).isEqualTo(1);

        verify(naverCollector, times(2))
                .collect("축구");

        verify(persistenceService).saveIfAbsent(
                eq(source),
                any(CollectedArticle.class),
                eq(List.of(interestId))
        );

        verify(retrySleeper)
                .sleep(1000L);
    }

    @Test
    @DisplayName("한 키워드 수집이 실패해도 다음 키워드는 계속 수집한다.")
    void 한_키워드가_실패해도_다음_키워드는_계속_수집한다() {
        // given
        UUID interestId = UUID.randomUUID();

        ArticleSource source = apiSource("NAVER");

        Interest interest = mock(Interest.class);
        Keyword failKeyword = mock(Keyword.class);
        Keyword successKeyword = mock(Keyword.class);

        given(interest.getId()).willReturn(interestId);
        givenArticleSaved(interestId, "스포츠");

        given(failKeyword.getInterest()).willReturn(interest);
        given(failKeyword.getKeyword()).willReturn("실패 키워드");

        given(successKeyword.getInterest()).willReturn(interest);
        given(successKeyword.getKeyword()).willReturn("성공 키워드");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of(failKeyword, successKeyword));

        NaverApiException nonRetryableException = new NaverApiException(
                400,
                "SE01",
                "잘못된 요청입니다.",
                false,
                new RuntimeException("잘못된 요청")
        );

        given(naverCollector.collect("실패 키워드"))
                .willThrow(nonRetryableException);

        given(naverCollector.collect("성공 키워드"))
                .willReturn(List.of(
                        article("성공 기사", "https://example.com/naver/success")
                ));

        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(interestId));

        given(subscriptionRepository.findUserIdsByInterestId(interestId))
                .willReturn(List.of());

        // when
        int savedCount = articleCollectService.collectAll();

        // then
        assertThat(savedCount).isEqualTo(1);

        verify(naverCollector).collect("실패 키워드");
        verify(naverCollector).collect("성공 키워드");

        verify(persistenceService).saveIfAbsent(
                eq(source),
                any(CollectedArticle.class),
                eq(List.of(interestId))
        );
    }

    @Test
    @DisplayName("재시도 가능한 네이버 오류가 계속되면 최대 세 번만 호출한다")
    void 재시도_가능한_네이버_오류는_최대_세_번만_호출한다() {
        // given
        ArticleSource source = apiSource("NAVER");

        Interest interest = mock(Interest.class);
        Keyword keyword = mock(Keyword.class);

        given(interest.getId()).willReturn(UUID.randomUUID());
        given(keyword.getInterest()).willReturn(interest);
        given(keyword.getKeyword()).willReturn("축구");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of(keyword));

        NaverApiException retryableException = new NaverApiException(
                500,
                "SE99",
                "네이버 API 시스템 오류",
                true,
                new RuntimeException("네이버 서버 오류")
        );

        given(naverCollector.collect("축구"))
                .willThrow(retryableException);

        // when & then
        assertThatThrownBy(articleCollectService::collectAll)
                .isInstanceOf(ArticleCollectException.class)
                .hasMessageContaining("모든 외부 기사 수집이 실패했습니다.");

        verify(naverCollector, times(3))
                .collect("축구");

        verifyNoInteractions(articleRepository);

        verify(retrySleeper, times(2))
                .sleep(1000L);
    }

    @Test
    @DisplayName("재시도 불가능한 네이버 오류는 한 번만 호출한다")
    void 재시도_불가능한_네이버_오류는_한_번만_호출한다() {
        // given
        ArticleSource source = apiSource("NAVER");

        Interest interest = mock(Interest.class);
        Keyword keyword = mock(Keyword.class);

        given(interest.getId()).willReturn(UUID.randomUUID());
        given(keyword.getInterest()).willReturn(interest);
        given(keyword.getKeyword()).willReturn("축구");

        given(articleSourceRepository.findAll())
                .willReturn(List.of(source));

        given(keywordRepository.findAllWithInterest())
                .willReturn(List.of(keyword));

        NaverApiException nonRetryableException = new NaverApiException(
                400,
                "SE01",
                "잘못된 요청입니다.",
                false,
                new RuntimeException("잘못된 요청")
        );

        given(naverCollector.collect("축구"))
                .willThrow(nonRetryableException);

        // when & then
        assertThatThrownBy(articleCollectService::collectAll)
                .isInstanceOf(ArticleCollectException.class)
                .hasMessageContaining("모든 외부 기사 수집이 실패했습니다.");

        verify(naverCollector, times(1))
                .collect("축구");

        verifyNoInteractions(articleRepository);

        verifyNoInteractions(retrySleeper);
    }

    @Test
    @DisplayName("기사 수집 서비스는 전체 수집 작업에 클래스 단위 트랜잭션을 사용하지 않는다")
    void 기사_수집_서비스는_클래스_단위_트랜잭션을_사용하지_않는다() {
        Transactional transactional =
                AnnotatedElementUtils.findMergedAnnotation(
                        ArticleCollectService.class,
                        Transactional.class
                );

        assertThat(transactional).isNull();
    }
}
