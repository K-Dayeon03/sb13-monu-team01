package com.project.monu.domain.batch.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "batch.scheduler.enabled", havingValue = "true")
public class ArticleCollectScheduler {

    private final JobOperator jobOperator;
    private final Job articleCollectJob;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void runCollectJob() {
        try {
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
