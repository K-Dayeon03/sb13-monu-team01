package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.exception.NaverApiException;
import com.project.monu.domain.article.collector.exception.NaverNetworkException;
import com.project.monu.domain.article.collector.exception.NaverResponseParseException;
import com.project.monu.domain.article.collector.naver.dto.NaverErrorResponse;
import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
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
        String body;

        try {
            body = naverRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("query", keyword)
                            .queryParam("display", 100)
                            .queryParam("sort", "date")
                            .build())
                    .retrieve()
                    .body(String.class);

        } catch (RestClientResponseException e) {
            throw convertApiException(e);

        } catch (ResourceAccessException e) {
            throw new NaverNetworkException("네이버 뉴스 API 연결에 실패했습니다.", e);
        }

        try {
            return objectMapper.readValue(
                    body,
                    NaverNewsResponse.class
            );
        } catch (JacksonException e) {
            throw new NaverResponseParseException(
                    "네이버 뉴스 API 응답 파싱에 실패했습니다.", e
            );
        }
    }

    private NaverApiException convertApiException(RestClientResponseException exception) {
        int statusCode = exception.getStatusCode().value();
        String errorCode = "UNKNOWN";
        String errorMessage = "네이버 뉴스 API 요청에 실패 했습니다.";

        try {
            NaverErrorResponse errorResponse = objectMapper.readValue(
                    exception.getResponseBodyAsString(),
                    NaverErrorResponse.class);
            if (errorResponse.errorCode() != null) {
                errorCode = errorResponse.errorCode();
            }

            if (errorResponse.errorMessage() != null) {
                errorMessage = errorResponse.errorMessage();
            }
        }catch (JacksonException parsingException) {
            // 오류 응답을 파싱하지 못해도 원래 HTTP 상태는 보존합니다.
        }

        return new NaverApiException(
                statusCode,
                errorCode,
                errorMessage,
                isRetryable(statusCode),
                exception
        );
    }

    private  boolean isRetryable(int statusCode) {
        return statusCode >= 500;
    }
}
