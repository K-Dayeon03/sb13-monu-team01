package com.project.monu.domain.batch.controller;

import com.project.monu.domain.article.dto.ArticleRestoreResultDto;
import com.project.monu.domain.article.service.ArticleBackupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BatchControllerTest {

    @Mock
    private JobOperator jobOperator;

    @Mock
    private Job articleCollectJob;

    @Mock
    private Job articleBackupJob;

    @Mock
    private ArticleBackupService articleBackupService;

    @Test
    @DisplayName("POST /api/batch/backup 요청 시 기사 백업 Job을 실행한다")
    void 백업_배치_수동_실행_요청을_처리한다() throws Exception {
        // given
        // Controller만 단독으로 테스트해서, HTTP 요청이 올바른 Job 실행으로 이어지는지만 확인합니다.
        MockMvc mockMvc = mockMvc();

        // when & then
        mockMvc.perform(post("/api/batch/backup"))
                .andExpect(status().isOk())
                .andExpect(content().string("기사 백업 배치 실행 완료"));

        ArgumentCaptor<JobParameters> paramsCaptor = ArgumentCaptor.forClass(JobParameters.class);
        verify(jobOperator).start(eq(articleBackupJob), paramsCaptor.capture());

        // Spring Batch는 같은 JobParameters로 같은 Job을 다시 실행하지 못하므로 timestamp를 넣어 매번 새 실행으로 만듭니다.
        assertThat(paramsCaptor.getValue().getLong("timestamp")).isNotNull();
    }

    @Test
    @DisplayName("POST /api/batch/restore 요청 시 지정 날짜 기사 복구를 실행한다")
    void 복구_수동_실행_요청을_처리한다() throws Exception {
        // given
        // 복구는 날짜 파라미터가 필요하므로, Controller가 LocalDate로 바인딩해 서비스에 넘기는지 확인합니다.
        LocalDate restoreDate = LocalDate.of(2026, 8, 20);
        given(articleBackupService.restore(restoreDate))
                .willReturn(new ArticleRestoreResultDto(
                        restoreDate,
                        "article-backups/2026-08-20.jsonl",
                        1
                ));

        // when & then
        mockMvc().perform(post("/api/batch/restore")
                        .param("date", "2026-08-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.restoreDate").value("2026-08-20"))
                .andExpect(jsonPath("$.backupKey").value("article-backups/2026-08-20.jsonl"))
                .andExpect(jsonPath("$.restoredArticleCount").value(1));

        verify(articleBackupService).restore(restoreDate);
    }

    private MockMvc mockMvc() {
        return MockMvcBuilders
                .standaloneSetup(new BatchController(
                        jobOperator,
                        articleCollectJob,
                        articleBackupJob,
                        articleBackupService
                ))
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
    }
}
