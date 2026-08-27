package com.project.monu.domain.article.collector.naver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class NaverClientConfigTest {

    @Test
    @DisplayName("네이버 API 요청에 connect timeout과 read timeout을 설정한다")
    void 네이버_API_요청에_timeout을_설정한다() {
        // given
        NaverClientConfig config = new NaverClientConfig();

        // when
        SimpleClientHttpRequestFactory requestFactory =
                config.naverRequestFactory();

        // then
        assertThat(ReflectionTestUtils.getField(requestFactory, "connectTimeout"))
                .isEqualTo(3_000);

        assertThat(ReflectionTestUtils.getField(requestFactory, "readTimeout"))
                .isEqualTo(10_000);
    }

}