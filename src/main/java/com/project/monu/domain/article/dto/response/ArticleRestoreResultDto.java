package com.project.monu.domain.article.dto.response;

import java.time.LocalDate;

/**
 * 복구 실행 결과를 내려주는 DTO입니다.
 *
 * 복구는 백업 파일과 현재 DB를 비교해 "새로 저장한 기사 수"가 핵심 결과이므로,
 * 복구 대상 날짜, 사용한 백업 key, 복구 건수를 함께 내려줍니다.
 */
public record ArticleRestoreResultDto(
        LocalDate restoreDate,
        String backupKey,
        long restoredArticleCount
) {
}
