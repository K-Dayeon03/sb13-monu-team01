package com.project.monu.domain.comment.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.service.CommentService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void 댓글을_등록한다() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

        CommentDto response = new CommentDto(
                commentId,
                articleId,
                userId,
                "댓글테스터",
                "댓글 등록 테스트입니다.",
                0L,
                false,
                Instant.parse("2026-08-24T00:00:00Z")
        );

        when(commentService.create(request)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "articleId": "%s",
                                  "userId": "%s",
                                  "content": "댓글 등록 테스트입니다."
                                }
                                """.formatted(articleId, userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.content").value("댓글 등록 테스트입니다."))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.likedByMe").value(false));

        verify(commentService).create(request);
    }

    @Test
    void 기사별_댓글_목록을_조회한다() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentDto comment = new CommentDto(
                commentId,
                articleId,
                userId,
                "댓글테스터",
                "댓글 내용입니다.",
                0L,
                false,
                Instant.parse("2026-08-24T00:00:00Z")
        );

        when(commentService.getComments(articleId)).thenReturn(List.of(comment));

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("articleId", articleId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].articleId").value(articleId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].userNickname").value("댓글테스터"))
                .andExpect(jsonPath("$[0].content").value("댓글 내용입니다."))
                .andExpect(jsonPath("$[0].likeCount").value(0))
                .andExpect(jsonPath("$[0].likedByMe").value(false));

        verify(commentService).getComments(articleId);
    }

    @Test
    void 댓글을_수정한다() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다.");

        CommentDto response = new CommentDto(
                commentId,
                articleId,
                userId,
                "댓글테스터",
                "수정된 댓글입니다.",
                0L,
                false,
                Instant.parse("2026-08-24T00:00:00Z")
        );

        when(commentService.update(commentId, userId, request)).thenReturn(response);

        // when & then
        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .header("Monew-Request-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "content": "수정된 댓글입니다."
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.content").value("수정된 댓글입니다."));

        verify(commentService).update(commentId, userId, request);
    }

    @Test
    void 댓글을_논리_삭제한다() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());

        verify(commentService).delete(commentId, userId);
    }

    @Test
    void 댓글_수정시_사용자_ID_헤더가_없으면_400을_응답한다() throws Exception {
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "content": "수정된 댓글입니다."
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 댓글_등록시_내용이_비어있으면_400을_응답한다() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "articleId": "%s",
                              "userId": "%s",
                              "content": ""
                            }
                            """.formatted(articleId, userId)))
                .andExpect(status().isBadRequest());
    }
}
