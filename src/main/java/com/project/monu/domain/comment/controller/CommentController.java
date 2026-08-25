package com.project.monu.domain.comment.controller;

import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.service.CommentService;
import com.project.monu.global.dto.CursorPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> create(
            @Valid @RequestBody CommentCreateRequest request) {

        CommentDto comment = commentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(comment);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<CommentDto>> getComments(
            @RequestParam(required = false) UUID articleId,
            @RequestParam String orderBy,
            @RequestParam String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Instant after,
            @RequestParam int limit,
            @RequestHeader("Monew-Request-User-ID") UUID requestUserId
    ) {
        CursorPageResponse<CommentDto> comments = commentService.getComments(
                articleId,
                orderBy,
                direction,
                cursor,
                after,
                limit,
                requestUserId
        );

        return ResponseEntity.ok(comments);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
            @PathVariable UUID commentId,
            @RequestHeader("Monew-Request-User-ID") UUID requestUserId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        CommentDto comment = commentService.update(commentId, requestUserId, request);

        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID commentId,
            @RequestHeader("Monew-Request-User-ID") UUID requestUserId
    ) {
        commentService.delete(commentId, requestUserId);

        return ResponseEntity.noContent().build();
    }

}