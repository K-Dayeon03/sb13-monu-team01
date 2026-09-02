package com.project.monu.global.logging;

import com.project.monu.global.constant.RequestHeaders;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void 요청_ID와_IP를_MDC와_응답_헤더에_추가한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/articles");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInChain = new AtomicReference<>();
        AtomicReference<String> clientIpInChain = new AtomicReference<>();

        request.addHeader(RequestHeaders.REQUEST_ID, "request-123");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");

        FilterChain filterChain = (servletRequest, servletResponse) -> {
            requestIdInChain.set(MDC.get(RequestLoggingFilter.MDC_REQUEST_ID));
            clientIpInChain.set(MDC.get(RequestLoggingFilter.MDC_CLIENT_IP));
        };

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(requestIdInChain.get()).isEqualTo("request-123");
        assertThat(clientIpInChain.get()).isEqualTo("203.0.113.10");
        assertThat(response.getHeader(RequestHeaders.REQUEST_ID)).isEqualTo("request-123");
        assertThat(response.getHeader(RequestHeaders.CLIENT_IP)).isEqualTo("203.0.113.10");

        // 요청 처리가 끝난 뒤에는 다음 요청에 값이 섞이지 않도록 MDC를 비웁니다.
        assertThat(MDC.get(RequestLoggingFilter.MDC_REQUEST_ID)).isNull();
        assertThat(MDC.get(RequestLoggingFilter.MDC_CLIENT_IP)).isNull();
    }

    @Test
    void 요청_ID가_없으면_새로_생성한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/articles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.setRemoteAddr("127.0.0.1");

        // when
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        // then
        assertThat(response.getHeader(RequestHeaders.REQUEST_ID)).isNotBlank();
        assertThat(response.getHeader(RequestHeaders.CLIENT_IP)).isEqualTo("127.0.0.1");
    }

    @Test
    void 전달받은_IP_헤더가_잘못되면_remoteAddr을_사용한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/articles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader("X-Forwarded-For", "not-an-ip");
        request.addHeader("X-Real-IP", "also-not-an-ip");
        request.setRemoteAddr("127.0.0.1");

        // when
        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        // then
        assertThat(response.getHeader(RequestHeaders.CLIENT_IP)).isEqualTo("127.0.0.1");
    }
}
