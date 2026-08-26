package com.project.monu.domain.article.dto;

import com.project.monu.domain.article.dto.response.ArticleRestoreResultDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleRestoreResultDtoTest {

    @Test
    void 복구_대상_날짜와_복구된_기사_ID와_복구_건수를_담는다() {
        // given
        Instant restoreDate = Instant.parse("2026-08-20T15:00:00Z");
        UUID restoredArticleId = UUID.randomUUID();

        // when
        ArticleRestoreResultDto dto = new ArticleRestoreResultDto(
                restoreDate,
                List.of(restoredArticleId),
                1
        );

        // then
        assertThat(dto.restoreDate()).isEqualTo(restoreDate);
        assertThat(dto.restoredArticleIds()).containsExactly(restoredArticleId);
        assertThat(dto.restoredArticleCount()).isEqualTo(1);
    }
}
