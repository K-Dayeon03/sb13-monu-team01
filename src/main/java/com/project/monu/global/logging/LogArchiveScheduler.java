package com.project.monu.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "batch.scheduler.enabled", havingValue = "true")
public class LogArchiveScheduler {

    private final LogArchiveService logArchiveService;

    public LogArchiveScheduler(LogArchiveService logArchiveService) {
        this.logArchiveService = logArchiveService;
    }

    /**
     * 매일 새벽에 전날 로그 파일을 저장소로 적재합니다.
     */
    @Scheduled(cron = "${log.archive.cron:0 30 2 * * *}", zone = "${log.archive.zone:Asia/Seoul}")
    public void archiveYesterdayLog() {
        try {
            logArchiveService.archiveYesterday();
        } catch (Exception e) {
            log.error("날짜별 로그 적재 실패", e);
        }
    }
}
