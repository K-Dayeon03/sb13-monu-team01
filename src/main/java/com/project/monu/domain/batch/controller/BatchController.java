package com.project.monu.domain.batch.controller;

import com.project.monu.domain.article.dto.ArticleRestoreResultDto;
import com.project.monu.domain.article.service.ArticleBackupService;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 기사 수집/백업/복구 작업을 수동으로 실행합니다.
 *
 * POST /api/batch/collect : 기사 수집 배치 실행
 * POST /api/batch/backup  : 전날 기사 백업 배치 실행
 * POST /api/batch/restore?date=2026-08-20 : 해당 날짜 백업 파일로 기사 복구 실행
 */


@RestController
@RequestMapping("/api/batch")
@ConditionalOnProperty(name = "batch.enabled", havingValue = "true")
public class BatchController {

    private final JobOperator jobOperator;
    private final Job articleCollectJob;
    private final Job articleBackupJob;
    private final ArticleBackupService articleBackupService;

    public BatchController(
            JobOperator jobOperator,
            @Qualifier("articleCollectJob") Job articleCollectJob,
            @Qualifier("articleBackupJob") Job articleBackupJob,
            ArticleBackupService articleBackupService
    ) {
        // Job Bean이 여러 개라서 @Qualifier로 수집 Job과 백업 Job을 명확히 구분합니다.
        this.jobOperator = jobOperator;
        this.articleCollectJob = articleCollectJob;
        this.articleBackupJob = articleBackupJob;
        this.articleBackupService = articleBackupService;
    }


    @PostMapping(value = "/collect", produces = "text/plain;charset=UTF-8")
    public String runCollectJob() throws Exception {
        // timestamp를 넣어 매번 다른 JobParameters를 만들면 같은 Job을 반복 실행할 수 있습니다.
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(articleCollectJob, params);
        return "배치 실행 완료";
    }

    @PostMapping(value = "/backup", produces = "text/plain;charset=UTF-8")
    public String runBackupJob() throws Exception {
        // 수동 실행도 스케줄 실행과 같은 articleBackupJob을 사용합니다.
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(articleBackupJob, params);
        return "기사 백업 배치 실행 완료";
    }

    @PostMapping("/restore")
    public ArticleRestoreResultDto restoreArticles(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        // 복구는 특정 날짜가 필요하므로 JobParameters 대신 서비스로 직접 연결합니다.
        // 나중에 복구도 완전한 Spring Batch Job으로 분리하면 이 메서드가 해당 Job 실행 API가 됩니다.
        return articleBackupService.restore(date);
    }
}
