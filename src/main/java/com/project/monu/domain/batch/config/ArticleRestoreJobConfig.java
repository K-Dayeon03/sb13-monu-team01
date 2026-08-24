package com.project.monu.domain.batch.config;

import com.project.monu.domain.article.service.ArticleBackupService;
import lombok.RequiredArgsConstructor;
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

import java.time.LocalDate;

@Configuration
@ConditionalOnProperty(name = "batch.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ArticleRestoreJobConfig {

    public static final String RESTORE_DATE_PARAM = "restoreDate";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final ArticleBackupService articleBackupService;

    @Bean
    public Job articleRestoreJob(Step articleRestoreStep) {
        return new JobBuilder("articleRestoreJob", jobRepository)
                .start(articleRestoreStep)
                .build();
    }

    @Bean
    public Step articleRestoreStep() {
        return new StepBuilder("articleRestoreStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String restoreDate = (String) chunkContext
                            .getStepContext()
                            .getJobParameters()
                            .get(RESTORE_DATE_PARAM);

                    articleBackupService.restore(LocalDate.parse(restoreDate));
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }
}
