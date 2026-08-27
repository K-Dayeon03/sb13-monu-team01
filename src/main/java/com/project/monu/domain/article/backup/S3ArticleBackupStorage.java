package com.project.monu.domain.article.backup;


import com.project.monu.global.config.ArticleBackupProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@ConditionalOnProperty(name = "article.backup.storage", havingValue = "s3")
@RequiredArgsConstructor
public class S3ArticleBackupStorage implements ArticleBackupStorage {

    private final S3Client s3Client;
    private final ArticleBackupProperties properties;

    @Override
    public void save(String key, String content) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(key)
                        .contentType("application/x-ndjson")
                        .build(),
                RequestBody.fromString(content)
        );
    }

    @Override
    public String load(String key) {
        ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(properties.bucket())
                        .key(key)
                        .build()
        );
        return response.asUtf8String();
    }

    @Override
    public boolean exists(String key) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(properties.bucket())
                            .key(key)
                            .build()
            );
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public String storageName() {
        return properties.bucket();
    }
}
