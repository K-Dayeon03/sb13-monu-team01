package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NaverNewsClientTest {

    @Test
    @Disabled("실제 Naver API 호출 - 필요할 때만 수동 실행")
    @DisplayName("네이버 뉴스 API를 실제로 호출해서 기사를 받아온다.")
    void 네이버_뉴스를_실제로_받아온다() {
        // given - RestClient 직접 만들기
        String clientId = System.getenv("NAVER_CLIENT_ID");
        String clientSecret = System.getenv("NAVER_CLIENT_SECRET");

        RestClient restClient = RestClient.builder()
                .baseUrl("https://naverapihub.apigw.ntruss.com/search/v1/news")
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        NaverNewsClient client = new NaverNewsClient(restClient, objectMapper);
        // when
        NaverNewsResponse response = client.search("치킨");

        // then
        assertThat(response).isNotNull();
        assertThat(response.items()).isNotEmpty();

        response.items().forEach(item -> {
            System.out.println("제목: " + item.title());
            System.out.println("링크: " + item.originalLink());
            System.out.println("날짜: " + item.pubDate());
            System.out.println("---");
        });
    }
}