package com.project.monu.domain.batch.tasklet;

import com.project.monu.domain.article.service.ArticleCollectService;
import com.project.monu.domain.batch.metrics.ArticleCollectMetrics;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ArticleCollectTasklet implements Tasklet {

    private final ArticleCollectService articleCollectService;
    private final ArticleCollectMetrics  articleCollectMetrics;

    public ArticleCollectTasklet(
            ArticleCollectService articleCollectService,
            ArticleCollectMetrics articleCollectMetrics){

        this.articleCollectService = articleCollectService;
        this.articleCollectMetrics = articleCollectMetrics;
    }



    @Override
    public @Nullable RepeatStatus execute(
            StepContribution contribution,
            ChunkContext chunkContext) {

        long startedAt = System.nanoTime();

        try {
            int savedCount = articleCollectService.collectAll();

            articleCollectMetrics.recordSuccess(
                    savedCount,
                    elapsedSince(startedAt)
            );

            return  RepeatStatus.FINISHED;
        } catch (RuntimeException exception) {
            articleCollectMetrics.recordFailure(
                    elapsedSince(startedAt)
            );

            throw exception;
        }
    }

    private Duration elapsedSince(long startedAt) {
        return Duration.ofNanos(System.nanoTime() - startedAt);
    }
}
