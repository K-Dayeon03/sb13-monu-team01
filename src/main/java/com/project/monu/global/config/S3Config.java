package com.project.monu.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@ConditionalOnProperty(name = "article.backup.storage", havingValue = "s3")
@EnableConfigurationProperties(ArticleBackupProperties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(ArticleBackupProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
