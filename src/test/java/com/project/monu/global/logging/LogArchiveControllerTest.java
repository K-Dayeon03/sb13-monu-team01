package com.project.monu.global.logging;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogArchiveControllerTest {

    @Test
    void 날짜를_지정해_로그_적재를_수동_실행한다() throws Exception {
        // given
        LogArchiveService logArchiveService = mock(LogArchiveService.class);
        LocalDate logDate = LocalDate.of(2026, 8, 28);

        when(logArchiveService.archive(logDate))
                .thenReturn(new LogArchiveService.LogArchiveResult(
                        logDate,
                        "logs/monu.2026-08-28.log",
                        "build/log-archives/2026/08/28/monu-2026-08-28.log",
                        true
                ));

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new LogArchiveController(logArchiveService))
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();

        // when & then
        mockMvc.perform(post("/api/batch/logs/archive")
                        .param("date", "2026-08-28"))
                .andExpect(status().isOk())
                .andExpect(content().string("로그 적재 완료: build/log-archives/2026/08/28/monu-2026-08-28.log"));
    }
}
