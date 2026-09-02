package com.project.monu.global.logging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "log.archive.storage", havingValue = "local", matchIfMissing = true)
public class LocalLogArchiveStorage implements LogArchiveStorage {

    private final LogArchivePath archivePath;

    public LocalLogArchiveStorage(LogArchivePath archivePath) {
        this.archivePath = archivePath;
    }

    /**
     * 로컬 테스트용 저장소입니다.
     *
     * <p>S3에 올리는 대신 build/log-archives 아래에 날짜 경로로 복사해서,
     * 파일 선택과 날짜별 적재 흐름을 AWS 없이 확인할 수 있습니다.</p>
     */
    @Override
    public ArchivedLog archive(LocalDate logDate, Path sourceFile) {
        Path destinationFile = archivePath.localDestinationFile(logDate);

        try {
            Files.createDirectories(destinationFile.getParent());
            Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return new ArchivedLog(logDate, destinationFile.toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to archive log locally.", e);
        }
    }

}
