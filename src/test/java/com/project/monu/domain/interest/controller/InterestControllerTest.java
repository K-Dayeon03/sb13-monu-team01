package com.project.monu.domain.interest.controller;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.request.InterestUpdateRequest;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @DisplayName("관심사 수정 요청이 성공하면 200과 수정된 InterestDto를 반환한다")
    void update_success() throws Exception {
        // given
        UUID interestId = UUID.randomUUID();
        InterestUpdateRequest request = new InterestUpdateRequest(List.of("머신러닝"));
        InterestDto response = new InterestDto(interestId, "인공지능", List.of("머신러닝"), 0L, false);
        when(interestService.update(eq(interestId), any())).thenReturn(response);

        // when & then
        mockMvc.perform(patch("/api/interests/{interestId}", interestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keywords[0]").value("머신러닝"));
    }

    @Test
    @DisplayName("관심사 삭제 요청이 성공하면 204를 반환한다")
    void delete_success() throws Exception {
        // given
        UUID interestId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/interests/{interestId}", interestId))
                .andExpect(status().isNoContent());

        verify(interestService).delete(interestId);
    }

    @Test
    @DisplayName("관심사 목록 조회 요청이 성공하면 200과 커서 페이지 응답을 반환한다")
    void getInterests_success() throws Exception {
        // given
        InterestDto interestDto = new InterestDto(UUID.randomUUID(), "인공지능", List.of("AI"), 0L, false);
        CursorPageResponse<InterestDto> response = new CursorPageResponse<>(
                List.of(interestDto), null, null, 10, 1L, false
        );
        when(interestService.getInterests(any(), any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/interests")
                        .param("keyword", "인공")
                        .param("orderBy", "subscriberCount")
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .header("MoNew-Request-User-ID", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("인공지능"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("관심사 목록 조회 시 orderBy 값이 잘못되면 400을 반환한다")
    void getInterests_returnsBadRequest_whenOrderByIsInvalid() throws Exception {
        // when & then
        mockMvc.perform(get("/api/interests")
                        .param("orderBy", "invalid")
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .header("MoNew-Request-User-ID", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("관심사 구독 요청이 성공하면 201과 SubscriptionDto를 반환한다")
    void subscribe_success() throws Exception {
        // given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        SubscriptionDto response = new SubscriptionDto(
                UUID.randomUUID(), interestId, "인공지능", List.of("AI"), 1L, Instant.now()
        );
        when(interestService.subscribe(userId, interestId)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
                        .header("MoNew-Request-User-ID", userId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.interestName").value("인공지능"));
    }

    @Test
    @DisplayName("관심사 구독취소 요청이 성공하면 200을 반환한다")
    void unsubscribe_success() throws Exception {
        // given
        UUID interestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
                        .header("MoNew-Request-User-ID", userId.toString()))
                .andExpect(status().isOk());

        verify(interestService).unsubscribe(userId, interestId);
    }

}
