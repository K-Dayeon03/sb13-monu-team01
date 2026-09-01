package com.project.monu.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Service
public class LogArchiveService {

    private final LogArchiveProperties properties;
    private final LogArchivePath archivePath;
    private final LogArchiveStorage storage;

    public LogArchiveService(
            LogArchiveProperties properties,
            LogArchivePath archivePath,
            LogArchiveStorage storage
    ) {
        this.properties = properties;
        this.archivePath = archivePath;
        this.storage = storage;
    }

    /**
     * 스케줄러에서 사용하는 기본 동작입니다.
     *
     * <p>자정이 지난 뒤 어제 날짜 로그 파일을 올리면, 오늘 아직 쓰는 중인 로그를
     * 건드리지 않아도 됩니다.</p>
     */
    public LogArchiveResult archiveYesterday() {
        LocalDate yesterday = LocalDate.now(ZoneId.of(properties.zone())).minusDays(1);
        return archive(yesterday);
    }

    /**
     * 지정한 날짜의 로그 파일을 저장소로 적재합니다.
     */
    public LogArchiveResult archive(LocalDate logDate) {
        Path sourceFile = resolveSourceFile(logDate);
        if (!Files.exists(sourceFile)) {
            log.warn("Log archive skipped because source file does not exist. date={} path={}", logDate, sourceFile);
            return LogArchiveResult.skipped(logDate, sourceFile.toString());
        }

        LogArchiveStorage.ArchivedLog archivedLog = storage.archive(logDate, sourceFile);
        log.info("Log archive completed. date={} location={}", logDate, archivedLog.location());

        return LogArchiveResult.archived(logDate, sourceFile.toString(), archivedLog.location());
    }

    private Path resolveSourceFile(LocalDate logDate) {
        return archivePath.sourceFile(logDate);
    }

    public record LogArchiveResult(
            LocalDate logDate,
            String sourceFile,
            String archivedLocation,
            boolean archived
    ) {

        private static LogArchiveResult archived(LocalDate logDate, String sourceFile, String archivedLocation) {
            return new LogArchiveResult(logDate, sourceFile, archivedLocation, true);
        }

        private static LogArchiveResult skipped(LocalDate logDate, String sourceFile) {
            return new LogArchiveResult(logDate, sourceFile, null, false);
        }
    }
}
