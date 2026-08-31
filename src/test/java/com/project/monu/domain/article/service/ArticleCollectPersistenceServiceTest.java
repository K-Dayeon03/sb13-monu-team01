package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.entity.ArticleInterest;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.interest.entity.Interest;
import jakarta.persistence.EntityManager;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ArticleCollectPersistenceServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ArticleCollectPersistenceService persistenceService;

    @Test
    @DisplayName("신규 기사는 기사와 관심사 연결을 하나의 저장 작업으로 처리한다.")
    void 신규_기사는_기사와_관심사_연결을_저장한다() {
        // given
        UUID interestId = UUID.randomUUID();
        Interest interest = Interest.create("축구");

        ArticleSource source = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://openapi.naver.com")
                .build();

        CollectedArticle collectedArticle = new CollectedArticle(
                "테스트 기사",
                "https://example.com/article/1",
                "테스트 요약",
                Instant.parse("2026-08-28T00:00:00Z")
        );

        given(articleRepository.existsBySourceUrl(collectedArticle.originalLink()))
                .willReturn(false);
        given(entityManager.getReference(Interest.class, interestId))
                .willReturn(interest);

        // when
        ArticleCollectPersistenceService.SaveResult result =
                persistenceService.saveIfAbsent(
                        source,
                        collectedArticle,
                        List.of(interestId)
                );

        // then
        assertThat(result.saved()).isTrue();
        assertThat(result.interestNames())
                .containsEntry(interestId, "축구");

        verify(articleRepository).save(any());
        verify(articleInterestRepository).save(any(ArticleInterest.class));


    }


}
