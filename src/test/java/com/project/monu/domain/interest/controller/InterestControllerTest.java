package com.project.monu.domain.interest.controller;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.service.InterestService;
import com.project.monu.global.dto.CursorPageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterestController.class)
class InterestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private InterestService interestService;

    @Test
    @DisplayName("관심사 등록 요청이 성공하면 201과 InterestDto를 반환한다")
    void register_success() throws Exception {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest("인공지능", List.of("AI"));
        InterestDto response = new InterestDto(UUID.randomUUID(), "인공지능", List.of("AI"), 0L, false);
        when(interestService.register(any())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("인공지능"));
    }

    @Test
    @DisplayName("관심사 목록 조회 요청이 성공하면 200과 커서 페이지 응답을 반환한다")
    void getInterests_success() throws Exception {
        // given
        InterestDto interestDto = new InterestDto(UUID.randomUUID(), "인공지능", List.of("AI"), 0L, false);
        CursorPageResponse<InterestDto> response = new CursorPageResponse<>(
                List.of(interestDto), null, null, 10, 1L, false
        );
        when(interestService.getInterests(any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/interests")
                        .param("keyword", "인공"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("인공지능"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }
}