package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.KeywordMatcher;
import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.exception.RssCollectException;
import com.project.monu.domain.article.collector.naver.NaverCollector;
import com.project.monu.domain.article.collector.rss.RssCollector;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
import com.project.monu.domain.interest.repository.KeywordRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
    @DisplayName("매칭된 기사는 저장된다")
    void 매칭된_기사는_저장된다() {
        // given
        ArticleSource source = rssSource("YEONHAP");
        given(articleSourceRepository.findAll()).willReturn(List.of(source));
        given(keywordRepository.findAllWithInterest()).willReturn(List.of());
        given(rssCollector.collect(anyString()))
                .willReturn(List.of(article("제목", "https://example.com/1")));
        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(UUID.randomUUID()));
        given(articleRepository.existsBySourceUrl(anyString())).willReturn(false);

        // when
        int saved = articleCollectService.collectAll();

        // then
        assertThat(saved).isEqualTo(1);
        verify(articleRepository, times(1)).save(any());
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
        given(articleRepository.existsBySourceUrl("https://example.com/dup"))
                .willReturn(true);
    
        // when
        int saved = articleCollectService.collectAll();
    
        // then
        assertThat(saved).isZero();
        verify(articleRepository, never()).save(any());
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
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("한 출처가 실패해도 나머지 출처는 계속 수집한다")
    void 한_출처가_실패해도_나머지는_계속_수집한다() {
        // given
        ArticleSource failSource = rssSource("FAIL");
        ArticleSource okSource = rssSource("OK");
        given(articleSourceRepository.findAll()).willReturn(List.of(failSource, okSource));
        given(keywordRepository.findAllWithInterest()).willReturn(List.of());
        given(rssCollector.collect(failSource.getSourceUrl()))
                .willThrow(new RssCollectException("RSS 실패", new RuntimeException("연결 실패")));
        given(rssCollector.collect(okSource.getSourceUrl()))
                .willReturn(List.of(article("제목", "https://example.com/ok")));
        given(keywordMatcher.findMatchedInterests(any(), any()))
                .willReturn(List.of(UUID.randomUUID()));
        given(articleRepository.existsBySourceUrl(anyString())).willReturn(false);

        // when
        int saved = articleCollectService.collectAll();

        // then
        assertThat(saved).isEqualTo(1);
        verify(articleRepository, times(1)).save(any());
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
}