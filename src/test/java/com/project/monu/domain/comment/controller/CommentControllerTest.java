package com.project.monu.domain.comment.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.monu.domain.comment.dto.CommentLikeDto;
import com.project.monu.global.constant.RequestHeaders;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.service.CommentService;
import com.project.monu.global.dto.CursorPageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private CommentService commentService;

    @Test
    void 댓글을_등록한다() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentCreateRequest request = new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

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
                        .content(jsonMapper.writeValueAsString(request)))
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
        UUID requestUserId = UUID.randomUUID();

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

        CursorPageResponse<CommentDto> response =
                CursorPageResponse.of(List.of(comment), null, null, 1, 1L, false);

        when(commentService.getComments(
                articleId, "createdAt", "DESC", null, null, 10, requestUserId
        )).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("articleId", articleId.toString())
                        .param("orderBy", "createdAt")
                        .param("direction", "DESC")
                        .param("limit", "10")
                        .header(RequestHeaders.REQUEST_USER_ID, requestUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$.content[0].articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.content[0].userNickname").value("댓글테스터"))
                .andExpect(jsonPath("$.content[0].content").value("댓글 내용입니다."))
                .andExpect(jsonPath("$.content[0].likeCount").value(0))
                .andExpect(jsonPath("$.content[0].likedByMe").value(false))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(commentService).getComments(
                articleId, "createdAt", "DESC", null, null, 10, requestUserId
        );
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
                        .header(RequestHeaders.REQUEST_USER_ID, userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
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
                        .header(RequestHeaders.REQUEST_USER_ID, userId.toString()))
                .andExpect(status().isNoContent());

        verify(commentService).delete(commentId, userId);
    }

    @Test
    void 댓글_수정시_사용자_ID_헤더가_없으면_400을_응답한다() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다.");

        // when & then
        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    void 댓글_등록시_내용이_비어있으면_400을_응답한다() throws Exception {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(articleId, userId, "");

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(commentService);
    }

    @Test
    void 댓글에_좋아요를_등록한다() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        UUID likeId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID commentUserId = UUID.randomUUID();

        Instant likeCreatedAt = Instant.parse("2026-08-27T06:00:00Z");
        Instant commentCreatedAt = Instant.parse("2026-08-26T06:00:00Z");

        CommentLikeDto response = new CommentLikeDto(
                likeId,
                requestUserId,
                likeCreatedAt,
                commentId,
                articleId,
                commentUserId,
                "작성자",
                "댓글 내용",
                1L,
                commentCreatedAt
        );

        when(commentService.like(commentId, requestUserId)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
                        .header(RequestHeaders.REQUEST_USER_ID, requestUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(likeId.toString()))
                .andExpect(jsonPath("$.likedBy").value(requestUserId.toString()))
                .andExpect(jsonPath("$.commentId").value(commentId.toString()))
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.commentUserId").value(commentUserId.toString()))
                .andExpect(jsonPath("$.commentUserNickname").value("작성자"))
                .andExpect(jsonPath("$.commentContent").value("댓글 내용"))
                .andExpect(jsonPath("$.commentLikeCount").value(1));

        verify(commentService).like(commentId, requestUserId);
    }
}
