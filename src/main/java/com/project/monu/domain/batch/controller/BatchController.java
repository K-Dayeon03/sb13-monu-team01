package com.project.monu.domain.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 기사 수집을 수동으로 처리 합니다.
 * postman -> (POST) http://localhost:8080/api/batch
 */


@RestController
@RequestMapping("/api/batch")
@ConditionalOnProperty(name = "batch.enabled", havingValue = "true")
@RequiredArgsConstructor
public class BatchController {

    private final JobOperator jobOperator;
    private final Job articleCollectJob;


    @PostMapping("/collect")
    public String runCollectJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobOperator.start(articleCollectJob, params);
        return "배치 실행 완료";
    }
}
