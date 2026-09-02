package com.project.monu.global.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * S3를 사용하는 기능들이 공유하는 AWS S3 설정입니다.
 *
 * <p>기사 백업과 로그 적재가 같은 버킷/리전을 사용할 수 있으므로,
 * 특정 도메인 이름 대신 공통 인프라 설정 이름으로 관리합니다.</p>
 */
@Validated
@ConfigurationProperties(prefix = "aws.s3")
public record AwsS3Properties(
        @NotBlank
        String bucket,
        @NotBlank
        String region
) {
}
