package com.project.monu.global.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/batch/logs")
@ConditionalOnProperty(name = "log.archive.manual-enabled", havingValue = "true")
public class LogArchiveController {

    private final LogArchiveService logArchiveService;

    public LogArchiveController(LogArchiveService logArchiveService) {
        this.logArchiveService = logArchiveService;
    }

    /**
     * 로컬 테스트나 운영 수동 재시도를 위해 특정 날짜 로그 적재를 직접 실행합니다.
     */
    @PostMapping(value = "/archive", produces = "text/plain;charset=UTF-8")
    public String archive(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        LogArchiveService.LogArchiveResult result = date == null
                ? logArchiveService.archiveYesterday()
                : logArchiveService.archive(date);

        if (!result.archived()) {
            return "로그 파일 없음: " + result.sourceFile();
        }

        return "로그 적재 완료: " + result.archivedLocation();
    }
}
