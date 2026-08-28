package com.project.monu.global.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LogArchiveServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void 날짜별_로그_파일을_로컬_저장소에_적재한다() throws Exception {
        // given
        Path logDir = tempDir.resolve("logs");
        Path archiveDir = tempDir.resolve("archives");
        LocalDate logDate = LocalDate.of(2026, 8, 28);
        Path logFile = logDir.resolve("monu." + logDate + ".log");

        Files.createDirectories(logDir);
        Files.writeString(logFile, "test log");

        LogArchiveProperties properties = new LogArchiveProperties(
                "local",
                logDir.toString(),
                archiveDir.toString(),
                "logs",
                "0 30 2 * * *",
                "Asia/Seoul",
                true
        );
        LogArchivePath archivePath = new LogArchivePath(properties);
        LogArchiveService service = new LogArchiveService(
                properties,
                archivePath,
                new LocalLogArchiveStorage(archivePath)
        );

        // when
        LogArchiveService.LogArchiveResult result = service.archive(logDate);

        // then
        Path archivedFile = archiveDir.resolve("2026/08/28/monu-2026-08-28.log");

        assertThat(result.archived()).isTrue();
        assertThat(result.archivedLocation()).isEqualTo(archivedFile.toString());
        assertThat(Files.readString(archivedFile)).isEqualTo("test log");
    }

    @Test
    void 로그_파일이_없으면_적재하지_않고_건너뛴다() {
        // given
        LogArchiveProperties properties = new LogArchiveProperties(
                "local",
                tempDir.resolve("logs").toString(),
                tempDir.resolve("archives").toString(),
                "logs",
                "0 30 2 * * *",
                "Asia/Seoul",
                true
        );
        LogArchivePath archivePath = new LogArchivePath(properties);
        LogArchiveService service = new LogArchiveService(
                properties,
                archivePath,
                new LocalLogArchiveStorage(archivePath)
        );

        // when
        LogArchiveService.LogArchiveResult result = service.archive(LocalDate.of(2026, 8, 28));

        // then
        assertThat(result.archived()).isFalse();
        assertThat(result.archivedLocation()).isNull();
    }
}
