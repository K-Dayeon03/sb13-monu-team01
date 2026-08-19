package com.project.monu.domain.article.service;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleCollectServiceTest {

    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleSourceRepository articleSourceRepository;
    @Mock
    private ArticleInterestRepository articleInterestRepository;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ArticleCollectService articleCollectService;

    @Test
    @DisplayName("이미 존재하는 링크의 기사는 저장하지 않는다")
    void 중복된_기사는_저장하지_않는다() {
        // given
        CollectedArticle article = new CollectedArticle(
                "제목", "https://example.com/1", "요약", Instant.now()
        );

        // 이미 존재 설정
        when(articleRepository.existsBySourceUrl("https://example.com/1"))
                .thenReturn(true);

        // when
        articleCollectService.save(article, List.of());

        // then
        verify(articleRepository, never()).save(any());
    }

    @Test
    @DisplayName("중복이 아닌 기사는 저장한다")
    void 중복이_아닌_기사는_저장된다() {
        // given
        CollectedArticle article = new CollectedArticle(
                "제목", "https://example.com/2", "요약", Instant.now()
        );
        when(articleRepository.existsBySourceUrl("https://example.com/2"))
                .thenReturn(false);
        ArticleSource naverSource = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://naver.com")
                .build();
        when(articleSourceRepository.findByName("NAVER"))
                .thenReturn(Optional.of(naverSource));

        // when
        articleCollectService.save(article, List.of());

        // then
        verify(articleRepository).save(any(Article.class));
    }
    
    @Test
    @DisplayName("매칭된 관심사들이 ArticleInterest로 저장된다")
    void 매칭된_관심사들을_저장한다() {
        // given
        CollectedArticle article = new CollectedArticle(
                "제목", "https://example.com/3", "요약", Instant.now()
        );
        when(articleRepository.existsBySourceUrl("https://example.com/3"))
                .thenReturn(false);
        ArticleSource naverSource = ArticleSource.builder()
                .name("NAVER").type(SourceType.API).sourceUrl("https://naver.com")
                .build();
        when(articleSourceRepository.findByName("NAVER"))
                .thenReturn(Optional.of(naverSource));
        when(entityManager.getReference(eq(Interest.class), any(UUID.class)))
                .thenReturn(mock(Interest.class));

        List<UUID> matchedInterestIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        // when
        articleCollectService.save(article, matchedInterestIds);
    
        // then
        verify(articleInterestRepository, times(2)).save(any());
    }
    

}