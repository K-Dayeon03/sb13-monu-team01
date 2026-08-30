package com.project.monu.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnExpression("'${article.backup.storage:}' == 's3' || '${log.archive.storage:}' == 's3'")
@EnableConfigurationProperties(AwsS3Properties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(AwsS3Properties properties) {
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
