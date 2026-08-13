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
}