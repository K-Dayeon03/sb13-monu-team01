package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.KeywordMatcher;
import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.naver.dto.InterestKeywords;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KeywordMatcherTest {

    @Test
    @DisplayName("제목에 키워드가 포함되면 true를 반환한다.")
    void 제목에_키워드가_포함되면_true() {
        // given
        KeywordMatcher matcher = new KeywordMatcher();
        CollectedArticle article = new CollectedArticle(
                "삼성전자 반도체 신제품",
                "https://example.com/1",
                "요약 내용",
                Instant.now()
        );

        // when
        boolean result = matcher.containsKeyword(article, "반도체");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("제목엔 없고 요약에만 키워드가 있어도 true를 반환한다")
    void 요약에_키워드가_포함되면_true() {
        // given
        KeywordMatcher matcher = new KeywordMatcher();
        CollectedArticle article = new CollectedArticle(
                "삼성전자 신제품",
                "https://example.com/1",
                "반도체 기술 개선",
                Instant.now()
        );

        // when
        boolean result = matcher.containsKeyword(article, "반도체");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("관심사의 키워드 중 하나라도 포함하면 매칭된다.")
    void 관심사_키워드_중_하나라도_포함되면_매칭() {
        // given
        KeywordMatcher matcher = new KeywordMatcher();
        CollectedArticle article = new CollectedArticle(
                "삼성전자 반도체 신제품",
                "https://example.com/1",
                "요약 내용",
                Instant.now()
        );
        InterestKeywords interest = new InterestKeywords(
                UUID.randomUUID(),
                List.of("인공지능", "반도체", "배터리")
        );

        // when
        boolean result = matcher.matchesInterest(article, interest);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("여러 관심사 중 매칭된 관심사들의 ID를 반환한다.")
    void 매칭된_관심사들의_ID를_반환한다() {
        // given
        KeywordMatcher matcher = new KeywordMatcher();
        CollectedArticle article = new CollectedArticle(
                "삼성전자 반도체 신제품",
                "https://example.com/1",
                "요약 내용",
                Instant.now()
        );
        UUID aiId = UUID.randomUUID();
        UUID semiconductorId = UUID.randomUUID();
        UUID sportsId = UUID.randomUUID();

        List<InterestKeywords> interests = List.of(
                new InterestKeywords(aiId, List.of("인공지능", "머신러닝")),       // 매칭 X
                new InterestKeywords(semiconductorId, List.of("반도체", "칩")),    // "반도체" 매칭
                new InterestKeywords(sportsId, List.of("축구", "야구"))            // 매칭 X
        );

        // when
        List<UUID> result = matcher.findMatchedInterests(article, interests);

        // then
        assertThat(result).containsExactly(semiconductorId);
    }


}
