package com.project.monu.domain.article.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;


/**
 * 기사 복구 DTO
 *
 * 백업(S3) 데이터와 현재 DB 데이터를 비교해 유실된 기사를 복구
 *
 * @param restoreDate 복구 대상 날짜
 * @param restoredArticleIds 복구된 기사 ID 목록
 * @param restoredArticleCount 복구된 기사 수
 */


public record ArticleRestoreResultDto(
        Instant restoreDate,
        List<UUID> restoredArticleIds,
        long restoredArticleCount
) {


    public static ArticleRestoreResultDto of(Instant restoreDate,
                                             List<UUID> restoredArticleIds) {
        return new ArticleRestoreResultDto(
                restoreDate,
                restoredArticleIds,
                restoredArticleIds.size()
        );
    }

}
