package com.project.monu.global.logging;

import java.nio.file.Path;
import java.time.LocalDate;

/**
 * 날짜별 로그 파일 이름과 적재 경로를 한곳에서 만듭니다.
 *
 * <p>로컬 저장소와 S3 저장소가 같은 날짜 경로 규칙을 공유해야,
 * 로컬 테스트 결과와 운영 S3 경로를 같은 방식으로 이해할 수 있습니다.</p>
 */
public class LogArchivePath {

    private final LogArchiveProperties properties;

    public LogArchivePath(LogArchiveProperties properties) {
        this.properties = properties;
    }

    public Path sourceFile(LocalDate logDate) {
        return Path.of(properties.sourceDir(), logFileName(logDate));
    }

    public Path localDestinationFile(LocalDate logDate) {
        return Path.of(
                properties.localDestinationDir(),
                datePath(logDate),
                archiveFileName(logDate)
        );
    }

    public String s3Key(LocalDate logDate) {
        String keyWithoutPrefix = datePath(logDate) + "/" + archiveFileName(logDate);
        String prefix = normalizedS3Prefix();

        if (prefix.isBlank()) {
            return keyWithoutPrefix;
        }

        return prefix + "/" + keyWithoutPrefix;
    }

    private String datePath(LocalDate logDate) {
        return logDate.getYear()
                + "/" + twoDigit(logDate.getMonthValue())
                + "/" + twoDigit(logDate.getDayOfMonth());
    }

    private String logFileName(LocalDate logDate) {
        return "monu." + logDate + ".log";
    }

    private String archiveFileName(LocalDate logDate) {
        return "monu-" + logDate + ".log";
    }

    private String normalizedS3Prefix() {
        return properties.s3Prefix().replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String twoDigit(int value) {
        return String.format("%02d", value);
    }
}
