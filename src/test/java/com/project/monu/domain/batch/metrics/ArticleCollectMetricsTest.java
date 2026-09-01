package com.project.monu.domain.batch.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleCollectMetricsTest {

    @Test
    void 기사_수집_성공_메트릭을_기록한다() {
        // given
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ArticleCollectMetrics metrics = new ArticleCollectMetrics(registry);

        // when
        metrics.recordSuccess(7, Duration.ofSeconds(2));

        // then
        assertThat(registry.get("monu.article.collect.executions")
                .tag("status", "success")
                .counter()
                .count())
                .isEqualTo(1.0);

        assertThat(registry.get("monu.article.collect.saved")
                .counter()
                .count())
                .isEqualTo(7.0);

        assertThat(registry.get("monu.article.collect.duration")
                .timer()
                .count())
                .isEqualTo(1L);

        assertThat(registry.get("monu.article.collect.duration")
                .timer()
                .totalTime(TimeUnit.SECONDS))
                .isEqualTo(2.0);
    }

    @Test
    void 기사_수집_실패_매트릭을_기록한다() {
        // given
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ArticleCollectMetrics metrics = new ArticleCollectMetrics(registry);


        // when
        metrics.recordFailure(Duration.ofMillis(1500));

        // then
        assertThat(registry.get("monu.article.collect.executions")
                .tag("status", "failure")
                .counter()
                .count())
                .isEqualTo(1.0);

        assertThat(registry.get("monu.article.collect.executions")
                .tag("status", "success")
                .counter()
                .count())
                .isZero();

        assertThat(registry.get("monu.article.collect.saved")
                .counter()
                .count())
                .isZero();

        assertThat(registry.get("monu.article.collect.duration")
                .timer()
                .count())
                .isEqualTo(1L);

        assertThat(registry.get("monu.article.collect.duration")
                .timer()
                .totalTime(TimeUnit.MILLISECONDS))
                .isEqualTo(1500.0);
    }

}