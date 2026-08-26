package com.project.monu.domain.article.collector.rss;

import com.project.monu.domain.article.collector.exception.RssCollectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

}