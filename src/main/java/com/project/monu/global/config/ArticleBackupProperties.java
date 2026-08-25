package com.project.monu.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws.s3")
public record ArticleBackupProperties(
        String bucket,
        String region
) {
}
