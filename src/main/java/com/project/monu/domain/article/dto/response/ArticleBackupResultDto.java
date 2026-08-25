package com.project.monu.domain.article.dto.response;

import java.time.LocalDate;

/**
 * 백업 실행 결과를 API나 테스트에서 확인하기 위한 응답 DTO입니다.
 *
 * storage는 현재 "local"이지만, S3 연동 후에는 bucket 이름이나 저장소 이름으로 확장할 수 있습니다.
 */
public record ArticleBackupResultDto(
        LocalDate backupDate,
        String storage,
        String key,
        long articleCount
) {
}
