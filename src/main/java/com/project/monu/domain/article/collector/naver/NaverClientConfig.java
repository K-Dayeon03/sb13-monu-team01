package com.project.monu.domain.article.collector.naver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NaverClientConfig {

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;


    @Bean
    public RestClient naverRestClient() {
        return RestClient.builder()
                .baseUrl("https://naverapihub.apigw.ntruss.com/search/v1/news")
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .build();
    }
}
