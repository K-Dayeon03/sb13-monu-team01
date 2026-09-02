package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.exception.RssCollectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.SocketTimeoutException;
import java.net.URLConnection;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RssClientTest {

    @Test
    @DisplayName("RSS 주소가 잘못되면 RSS 수집 예외를 던진다.")
    void RSS_주소가_잘못되면_RSS_수집_예외를_던진다() {
        // given
        RssClient rssClient = new RssClient();

        // when & then
        assertThatThrownBy(()-> rssClient.fetch("invalid-url"))
                .isInstanceOf(RssCollectException.class)
                .hasMessageContaining("RSS 피드");

    }

    @Test
    @DisplayName("RSS 연결에 connect timeout과 read timeout을 설정한다.")
    void RSS_연결에_timeout을_설정한다() throws Exception{
        // given
        String url = "https://example.com/rss";

        RssConnectionFactory connectionFactory = mock(RssConnectionFactory.class);

        URLConnection connection = mock(URLConnection.class);

        given(connectionFactory.open(url))
                .willReturn(connection);

        given(connection.getInputStream())
                .willThrow(new SocketTimeoutException("Read timed out"));

        RssClient rssClient = new RssClient(
                connectionFactory,
                3_000,
                10_000
        );

        // when & then
        assertThatThrownBy(()-> rssClient.fetch(url))
                .isInstanceOf(RssCollectException.class)
                .hasCauseInstanceOf(SocketTimeoutException.class);

        verify(connection).setConnectTimeout(3_000);
        verify(connection).setReadTimeout(10_000);
    }

}