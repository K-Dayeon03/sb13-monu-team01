package com.project.monu.domain.batch.tasklet;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.service.ArticleCollectService;
import com.project.monu.domain.batch.metrics.ArticleCollectMetrics;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleCollectTaskletTest {

    private final ArticleCollectService articleCollectService
            = mock(ArticleCollectService.class);

    private final ArticleCollectMetrics articleCollectMetrics
            = mock(ArticleCollectMetrics.class);

    private final ArticleCollectTasklet tasklet =
            new ArticleCollectTasklet(
                    articleCollectService,
                    articleCollectMetrics
            );


    @Test
    void 기사_수집에_성공하면_저장_건수와_실행_시간을_기록한다() throws Exception {
        // given
        when(articleCollectService.collectAll()).thenReturn(7);

        // when
        RepeatStatus result = tasklet.execute(null, null);

        // then
        assertThat(result).isEqualTo(RepeatStatus.FINISHED);

        verify(articleCollectMetrics).recordSuccess(
                eq(7),
                any(Duration.class)
        );
        verify(articleCollectMetrics, never())
                .recordFailure(any(Duration.class));
    }

    @Test
    void 기사_수집_실패하면_실패_횟수와_실패_시간을_기록하고_예외를_전파한다() {

        // given
        RuntimeException failure = new RuntimeException("기사 수집 실패");

        when(articleCollectService.collectAll())
                .thenThrow(failure);

        // when & then
        assertThatThrownBy(() -> tasklet.execute(null, null))
                .isSameAs(failure);

        verify(articleCollectMetrics)
                .recordFailure(any(Duration.class));

        verify(articleCollectMetrics, never())
                .recordSuccess(
                        any(Integer.class),
                        any(Duration.class)
                );
    }

}