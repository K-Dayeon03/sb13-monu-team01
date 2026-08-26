package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class NaverNewsClientTest {

    private static final Logger log = LoggerFactory.getLogger(NaverNewsClientTest.class);


    @Test
    @EnabledIfSystemProperty(
            named = "naver.real-api-test",
            matches = "true",
            disabledReason = "실제 Naver API 호출은 -Dnaver.real-api-test=true 옵션을 켰을 때만 실행"
    )
    @DisplayName("네이버 뉴스 API를 실제로 호출해서 기사를 받아온다.")
    void 네이버_뉴스를_실제로_받아온다() {
        // given - RestClient 직접 만들기
        String clientId = System.getenv("NAVER_CLIENT_ID");
        String clientSecret = System.getenv("NAVER_CLIENT_SECRET");
        assumeFalse(isBlank(clientId), "NAVER_CLIENT_ID 환경변수가 필요합니다.");
        assumeFalse(isBlank(clientSecret), "NAVER_CLIENT_SECRET 환경변수가 필요합니다.");

        RestClient restClient = RestClient.builder()
                .baseUrl("https://naverapihub.apigw.ntruss.com/search/v1/news")
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        NaverNewsClient client = new NaverNewsClient(restClient, objectMapper);
        // when
        NaverNewsResponse response = client.search("유튜브");

        // then
        assertThat(response).isNotNull();
        assertThat(response.items()).isNotEmpty();

        response.items().forEach(item -> {
            log.info("제목: {}", item.title());
            log.info("링크: {}", item.originalLink());
            log.info("날짜: {}", item.pubDate());
            log.info("---");
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
