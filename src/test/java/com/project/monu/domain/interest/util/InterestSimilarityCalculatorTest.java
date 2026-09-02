package com.project.monu.domain.interest.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class InterestSimilarityCalculatorTest {

    @Test
    void 완전히_동일한_문자열은_유사도가_1이다() {
        double similarity = InterestSimilarityCalculator.calculate("스포츠", "스포츠");
        assertThat(similarity).isEqualTo(1.0);
    }

    @ParameterizedTest
    @CsvSource({
            "국내여행정보, 국내여행정부, true",
            "스포츠, 경제, false"

    })
    void 유사도가_80퍼센트_이상이면_유사하다고_판단한다(String a, String b, boolean expected) {
        boolean result = InterestSimilarityCalculator.isSimilar(a, b);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "AI, BI, true",     // 길이 2, 편집 거리 1 -> 비율(0.5)로는 놓치지만 절대 거리 기준으로 유사 판정
            "2026, 2027, true", // 길이 4(경계값), 편집 거리 1 -> 비율(0.75)로는 놓치지만 절대 거리 기준으로 유사 판정
            "BTS, BFS, true",   // 길이 3, 편집 거리 1
            "AI, IT, false"     // 길이 2, 편집 거리 2 -> 유사하지 않음
    })
    void 짧은_이름은_비율_대신_절대_편집_거리로_유사_여부를_판단한다(String a, String b, boolean expected) {
        boolean result = InterestSimilarityCalculator.isSimilar(a, b);
        assertThat(result).isEqualTo(expected);
    }
}