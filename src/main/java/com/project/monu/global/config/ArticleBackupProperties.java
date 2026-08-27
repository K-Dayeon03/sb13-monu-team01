package com.project.monu.global.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "aws.s3")
public record ArticleBackupProperties(
        @NotBlank
        String bucket,
        @NotBlank
        String region
) {
}
