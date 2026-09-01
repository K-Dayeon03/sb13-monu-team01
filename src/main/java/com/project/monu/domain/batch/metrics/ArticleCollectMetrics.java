package com.project.monu.domain.batch.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ArticleCollectMetrics {

    private static final String EXECUTIONS_METRIC =
            "monu.article.collect.executions";
    private static final String SAVED_METRIC =
            "monu.article.collect.saved";
    private static final String DURATION_METRIC =
            "monu.article.collect.duration";

    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter savedArticleCounter;
    private final Timer durationTimer;


    public ArticleCollectMetrics(MeterRegistry registry) {
        this.successCounter = Counter.builder(EXECUTIONS_METRIC)
                .tag("status", "success")
                .description("기사 수집 성공 횟수")
                .register(registry);

        this.failureCounter = Counter.builder(EXECUTIONS_METRIC)
                .tag("status", "failure")
                .description("기사 수집 실패 횟수")
                .register(registry);

        this.savedArticleCounter = Counter.builder(SAVED_METRIC)
                .description("저장된 기사 누적 수")
                .register(registry);

        this.durationTimer = Timer.builder(DURATION_METRIC)
                .description("기사 수집 실행 시간")
                .register(registry);
    }


    public void recordSuccess(int savedCount, Duration duration) {
        successCounter.increment();
        savedArticleCounter.increment(savedCount);
        durationTimer.record(duration);
    }

    public void recordFailure(Duration duration) {
        failureCounter.increment();
        durationTimer.record(duration);
    }

}
