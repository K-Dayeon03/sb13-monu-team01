package com.project.monu.global.logging;

import com.project.monu.global.config.AwsS3Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "log.archive.storage", havingValue = "s3")
public class S3LogArchiveStorage implements LogArchiveStorage {

    private static final String CONTENT_TYPE = "text/plain; charset=utf-8";

    private final S3Client s3Client;
    private final AwsS3Properties s3Properties;
    private final LogArchivePath archivePath;

    public S3LogArchiveStorage(
            S3Client s3Client,
            AwsS3Properties s3Properties,
            LogArchivePath archivePath
    ) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
        this.archivePath = archivePath;
    }

    /**
     * 날짜별 로그 파일을 S3의 날짜 경로에 적재합니다.
     */
    @Override
    public ArchivedLog archive(LocalDate logDate, Path sourceFile) {
        String key = archivePath.s3Key(logDate);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(CONTENT_TYPE)
                .build();

        s3Client.putObject(request, RequestBody.fromFile(sourceFile));

        return new ArchivedLog(logDate, "s3://" + s3Properties.bucket() + "/" + key);
    }
}
