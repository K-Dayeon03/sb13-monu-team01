package com.project.monu.domain.article.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 복구 실행 결과를 내려주는 DTO입니다.
 *
 * 복구는 백업 파일과 현재 DB를 비교해 새로 저장한 기사 정보를 내려줍니다.
 */
public record ArticleRestoreResultDto(
        Instant restoreDate,
        List<UUID> restoredArticleIds,
        long restoredArticleCount
) {
}
