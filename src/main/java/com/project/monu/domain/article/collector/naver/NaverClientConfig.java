package com.project.monu.domain.article.collector.naver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class NaverClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 3_000;
    private static final int READ_TIMEOUT_MILLIS = 10_000;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    @Bean
    public SimpleClientHttpRequestFactory naverRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();


        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MILLIS);

        return requestFactory;
    }


    @Bean
    public RestClient naverRestClient() {
        return RestClient.builder()
                .baseUrl("https://naverapihub.apigw.ntruss.com/search/v1/news")
                .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .requestFactory(naverRequestFactory())
                .build();
    }
}
