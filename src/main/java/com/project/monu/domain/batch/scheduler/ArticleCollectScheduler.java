package com.project.monu.domain.batch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "batch.scheduler.enabled", havingValue = "true")
public class ArticleCollectScheduler {

    private final JobOperator jobOperator;
    private final Job articleCollectJob;

    public ArticleCollectScheduler(
            JobOperator jobOperator,
            @Qualifier("articleCollectJob") Job articleCollectJob
    ) {
        // Job Bean이 2개 이상이므로 수집 스케줄러는 articleCollectJob만 실행하도록 고정합니다.
        this.jobOperator = jobOperator;
        this.articleCollectJob = articleCollectJob;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void runCollectJob() {
        try {
            // 같은 JobInstance 재실행 오류를 피하려고 스케줄 실행마다 timestamp를 새로 넣습니다.
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobOperator.start(articleCollectJob, params);
            log.info(">>> 기사 수집 배치 스케줄 실행");
        } catch (Exception e) {
            log.error("기사 수집 배치 스케줄 실행 실패", e);
        }

    }
}
