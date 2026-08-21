package com.project.monu.domain.comment.controller;

import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<List<CommentDto>> getComments(
            @RequestParam UUID articleId
    ) {
        List<CommentDto> comments = commentService.getComments(articleId);
        return ResponseEntity.ok(comments);
    }

}