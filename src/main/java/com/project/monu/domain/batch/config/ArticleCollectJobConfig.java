package com.project.monu.domain.batch.config;

import com.project.monu.domain.batch.tasklet.ArticleCollectTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.EnableJdbcJobRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository
@ConditionalOnProperty(name = "batch.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ArticleCollectJobConfig {

    private final JobRepository  jobRepository;
    private final PlatformTransactionManager txManager;
    private final ArticleCollectTasklet  articleCollectTasklet;

    @Bean
    public Job articleCollectJob(Step articleCollectStep) {
        return new JobBuilder("articleCollectJob", jobRepository)
                .start(articleCollectStep)
                .build();
    }

    @Bean
    public Step articleCollectStep() {
        DefaultTransactionAttribute transactionAttribute =
                new DefaultTransactionAttribute();
        transactionAttribute.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_NOT_SUPPORTED
        );

        return new StepBuilder("articleCollectStep", jobRepository)
                .tasklet(articleCollectTasklet, txManager)
                .transactionAttribute(transactionAttribute)
                .build();
    }
}
