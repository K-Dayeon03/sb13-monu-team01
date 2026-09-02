package com.project.monu.domain.article.dto.response;

import java.time.LocalDate;

/**
 * 백업 실행 결과를 API나 테스트에서 확인하기 위한 응답 DTO입니다.
 *
 * storage는 로컬 실행에서는 "local", S3 실행에서는 bucket 이름입니다.
 */
public record ArticleBackupResultDto(
        LocalDate backupDate,
        String storage,
        String key,
        long articleCount
) {
}
