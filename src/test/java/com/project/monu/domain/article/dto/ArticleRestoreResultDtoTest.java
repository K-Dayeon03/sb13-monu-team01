package com.project.monu.domain.article.dto;

import com.project.monu.domain.article.dto.response.ArticleRestoreResultDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleRestoreResultDtoTest {

    @Test
    void 복구_대상_날짜와_백업_key와_복구_건수를_담는다() {
        // given
        LocalDate restoreDate = LocalDate.of(2026, 8, 20);
        String backupKey = "article-backups/2026-08-20.jsonl";

        // when
        ArticleRestoreResultDto dto = new ArticleRestoreResultDto(restoreDate, backupKey, 2);

        // then
        assertThat(dto.restoreDate()).isEqualTo(restoreDate);
        assertThat(dto.backupKey()).isEqualTo(backupKey);
        assertThat(dto.restoredArticleCount()).isEqualTo(2);
    }
}
