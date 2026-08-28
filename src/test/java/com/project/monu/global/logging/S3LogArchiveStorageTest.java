package com.project.monu.global.logging;

import com.project.monu.global.config.AwsS3Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class S3LogArchiveStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void 날짜별_경로로_S3에_로그를_업로드한다() throws Exception {
        // given
        S3Client s3Client = mock(S3Client.class);
        Path logFile = tempDir.resolve("monu.2026-08-28.log");
        Files.writeString(logFile, "test log");
        LogArchiveProperties properties = new LogArchiveProperties(
                "s3",
                "logs",
                "build/log-archives",
                "/app-logs/",
                "0 30 2 * * *",
                "Asia/Seoul",
                false
        );

        S3LogArchiveStorage storage = new S3LogArchiveStorage(
                s3Client,
                new AwsS3Properties("monu-bucket", "ap-northeast-2"),
                new LogArchivePath(properties)
        );

        // when
        LogArchiveStorage.ArchivedLog archivedLog = storage.archive(LocalDate.of(2026, 8, 28), logFile);

        // then
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        assertThat(requestCaptor.getValue().bucket()).isEqualTo("monu-bucket");
        assertThat(requestCaptor.getValue().key()).isEqualTo("app-logs/2026/08/28/monu-2026-08-28.log");
        assertThat(archivedLog.location()).isEqualTo("s3://monu-bucket/app-logs/2026/08/28/monu-2026-08-28.log");
    }
}
