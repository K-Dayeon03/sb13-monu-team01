package com.project.monu.global.logging;

import com.project.monu.global.constant.RequestHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.UUID;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_CLIENT_IP = "clientIp";

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)"
                    + "(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$"
    );
    private static final Pattern IPV6_PATTERN = Pattern.compile("^[0-9a-fA-F:]+$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        String clientIp = resolveClientIp(request);
        long startedAt = System.currentTimeMillis();

        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_CLIENT_IP, clientIp);

        response.setHeader(RequestHeaders.REQUEST_ID, requestId);
        response.setHeader(RequestHeaders.CLIENT_IP, clientIp);

        try {
            log.info("HTTP request started method={} uri={}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMillis = System.currentTimeMillis() - startedAt;
            log.info(
                    "HTTP request finished method={} uri={} status={} elapsedMillis={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    elapsedMillis
            );
            MDC.remove(MDC_REQUEST_ID);
            MDC.remove(MDC_CLIENT_IP);
        }
    }

    /**
     * 클라이언트가 이미 요청 ID를 보냈으면 그대로 이어받고, 없으면 서버에서 새로 만듭니다.
     */
    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(RequestHeaders.REQUEST_ID);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return requestId;
    }

    /**
     * 프록시 환경에서는 X-Forwarded-For의 첫 번째 IP가 원 클라이언트에 가장 가깝습니다.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String forwardedClientIp = forwardedFor.split(",")[0].trim();
            if (isValidIp(forwardedClientIp)) {
                return forwardedClientIp;
            }
        }

        String realIp = request.getHeader(X_REAL_IP);
        if (isValidIp(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    private boolean isValidIp(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        return IPV4_PATTERN.matcher(value).matches()
                || (value.contains(":") && IPV6_PATTERN.matcher(value).matches());
    }
}
