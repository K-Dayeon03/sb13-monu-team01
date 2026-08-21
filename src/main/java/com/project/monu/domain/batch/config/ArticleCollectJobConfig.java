package com.project.monu.domain.batch.config;

import com.project.monu.domain.article.service.ArticleCollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository
@ConditionalOnProperty(name = "batch.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ArticleCollectJobConfig {

    private final JobRepository  jobRepository;
    private final PlatformTransactionManager txManager;
    private final ArticleCollectService articleCollectService;

    @Bean
    public Job articleCollectJob(Step articleCollectStep) {
        return new JobBuilder("articleCollectJob", jobRepository)
                .start(articleCollectStep)
                .build();
    }

    @Bean
    public Step articleCollectStep() {
        return new StepBuilder("articleCollectStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    articleCollectService.collectAll();
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }
}
