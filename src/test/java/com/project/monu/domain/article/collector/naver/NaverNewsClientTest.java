package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.exception.NaverApiException;
import com.project.monu.domain.article.collector.exception.NaverNetworkException;
import com.project.monu.domain.article.collector.exception.NaverResponseParseException;
import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

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

        NaverNewsResponse response = client.search("유튜브");

        assertThat(response).isNotNull();
        assertThat(response.items()).isNotEmpty();

        response.items().forEach(item -> {
            log.info("제목: {}", item.title());
            log.info("링크: {}", item.originalLink());
            log.info("날짜: {}", item.pubDate());
            log.info("---");
        });
    }

    @Test
    @DisplayName("네이버 API가 SE02 오류를 응답하면 재시도 불가능한 API 예외를 던진다")
    void 네이버_API가_SE02를_응답하면_재시도_불가능한_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://naver.test");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        server.expect(queryParam("query", "football"))
                .andExpect(queryParam("display", "100"))
                .andExpect(queryParam("sort", "date"))
                .andRespond(
                        withStatus(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("""
                                        {
                                          "errorCode": "SE02",
                                          "errorMessage": "Invalid display value"
                                        }
                                        """)
                );

        NaverNewsClient client = new NaverNewsClient(
                builder.build(),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.search("football"))
                .isInstanceOfSatisfying(
                        NaverApiException.class,
                        exception -> {
                            assertThat(exception.getStatusCode())
                                    .isEqualTo(400);
                            assertThat(exception.getErrorCode())
                                    .isEqualTo("SE02");
                            assertThat(exception.isRetryable())
                                    .isFalse();
                        }
                );

        server.verify();
    }

    @Test
    @DisplayName("네이버 API가 SE99 오류를 응답하면 재시도 가능한 API 예외를 던진다")
    void 네이버_API가_SE99를_응답하면_재시도_가능한_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://naver.test");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        server.expect(queryParam("query", "football"))
                .andExpect(queryParam("display", "100"))
                .andExpect(queryParam("sort", "date"))
                .andRespond(
                        withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("""
                                        {
                                          "errorCode": "SE99",
                                          "errorMessage": "System Error"
                                        }
                                        """)
                );

        NaverNewsClient client = new NaverNewsClient(
                builder.build(),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.search("football"))
                .isInstanceOfSatisfying(
                        NaverApiException.class,
                        exception -> {
                            assertThat(exception.getStatusCode())
                                    .isEqualTo(500);
                            assertThat(exception.getErrorCode())
                                    .isEqualTo("SE99");
                            assertThat(exception.isRetryable())
                                    .isTrue();
                        }
                );

        server.verify();
    }

    @Test
    @DisplayName("네이버 API 호출 한도를 초과하면 재시도 불가능한 API 예외를 던진다.")
    void 네이버_API_호출_한도를_초과하면_재시도_불가능한_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://naver.test");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        server.expect(queryParam("query", "football"))
                .andExpect(queryParam("display", "100"))
                .andExpect(queryParam("sort", "date"))
                .andRespond(
                        withStatus(HttpStatus.TOO_MANY_REQUESTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("""
                                        {
                                          "errorCode": "429",
                                          "errorMessage": "Rate limit exceeded"
                                        }
                                        """)
                );

        NaverNewsClient client = new NaverNewsClient(
                builder.build(),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.search("football"))
                .isInstanceOfSatisfying(
                        NaverApiException.class,
                        exception -> {
                            assertThat(exception.getStatusCode())
                                    .isEqualTo(429);
                            assertThat(exception.getErrorCode())
                                    .isEqualTo("429");
                            assertThat(exception.isRetryable())
                                    .isFalse();
                        }
                );

        server.verify();
    }

    @Test
    @DisplayName("네이버 API의 정상 응답 JSON이 잘못되면 응답 파싱 예외를 던진다")
    void 네이버_API의_정상_응답_JSON이_잘못되면_응답_파싱_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://naver.test");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        server.expect(queryParam("query", "football"))
                .andExpect(queryParam("display", "100"))
                .andExpect(queryParam("sort", "date"))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{ invalid-json }")
                );

        NaverNewsClient client = new NaverNewsClient(
                builder.build(),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.search("football"))
                .isInstanceOf(NaverResponseParseException.class)
                .hasMessageContaining("파싱");

        server.verify();
    }

    @Test
    @DisplayName("네이버 API 연결에 실패하면 네트워크 예외를 던진다.")
    void 네이버_API_연결에_실패하면_네트워크_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://naver.test");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        server.expect(queryParam("query", "football"))
                .andExpect(queryParam("display", "100"))
                .andExpect(queryParam("sort", "date"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection timed out");
                });

        NaverNewsClient client = new NaverNewsClient(
                builder.build(),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.search("football"))
                .isInstanceOf(NaverNetworkException.class)
                .hasMessageContaining("연결");

        server.verify();
    }

    @Test
    @DisplayName("네이버 오류 응답을 파싱하지 못해도 HTTP 상태를 보존한다.")
    void 네이버_오류_응답을_파싱하지_못해도_HTTP_상태를_보존한다() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://naver.test");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        server.expect(queryParam("query", "football"))
                .andExpect(queryParam("display", "100"))
                .andExpect(queryParam("sort", "date"))
                .andRespond(
                        withStatus(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{ invalid-error-json }")
                );

        NaverNewsClient client = new NaverNewsClient(
                builder.build(),
                new ObjectMapper()
        );

        assertThatThrownBy(() -> client.search("football"))
                .isInstanceOfSatisfying(
                        NaverApiException.class,
                        exception -> {
                            assertThat(exception.getStatusCode())
                                    .isEqualTo(400);
                            assertThat(exception.getErrorCode())
                                    .isEqualTo("UNKNOWN");
                            assertThat(exception.isRetryable())
                                    .isFalse();
                            assertThat(exception.getMessage())
                                    .contains("네이버 뉴스 API 요청");
                        }
                );

        server.verify();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
