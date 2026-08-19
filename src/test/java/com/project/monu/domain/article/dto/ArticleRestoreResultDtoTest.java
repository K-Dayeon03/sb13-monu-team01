package com.project.monu.domain.article.dto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleRestoreResultDtoTest {

    @Test
    void 복구된_기사_ID_목록으로_복구_결과_DTO를_생성한다() {
        // given
        // 복구 작업이 끝난 뒤, 복구된 기사 ID 목록만 알면 응답용 count는 DTO에서 계산합니다.
        Instant restoreDate = Instant.parse("2026-08-19T00:00:00Z");
        List<UUID> restoredArticleIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        // when
        ArticleRestoreResultDto dto = ArticleRestoreResultDto.of(restoreDate, restoredArticleIds);

        // then
        assertThat(dto.restoreDate()).isEqualTo(restoreDate);
        assertThat(dto.restoredArticleIds()).containsExactlyElementsOf(restoredArticleIds);
        assertThat(dto.restoredArticleCount()).isEqualTo(2);
    }
}
