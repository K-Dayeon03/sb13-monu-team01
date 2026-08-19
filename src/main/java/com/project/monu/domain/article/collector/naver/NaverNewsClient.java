package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class NaverNewsClient {

    private final RestClient naverRestClient;
    private final ObjectMapper objectMapper;

    public NaverNewsClient(RestClient naverRestClient, ObjectMapper objectMapper) {
        this.naverRestClient = naverRestClient;
        this.objectMapper = objectMapper;
    }

    public NaverNewsResponse search(String keyword) {
        String body = naverRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", keyword)
                        .queryParam("display", 100)
                        .queryParam("sort", "date")
                        .build())
                .retrieve()
                .body(String.class);   // ← String으로 먼저 받음

        try {
            return objectMapper.readValue(body, NaverNewsResponse.class);  // ← 직접 JSON 파싱
        } catch (Exception e) {
            throw new RuntimeException("네이버 응답 파싱 실패: " + body, e);
        }
    }
}
