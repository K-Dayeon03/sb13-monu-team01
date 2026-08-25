package com.project.monu.domain.interest.controller;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.domain.interest.dto.request.InterestUpdateRequest;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.interest.service.InterestService;
import com.project.monu.global.constant.RequestHeaders;
import com.project.monu.global.dto.CursorPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestService interestService;

    @PostMapping
    public ResponseEntity<InterestDto> register(@Valid @RequestBody InterestRegisterRequest request) {
        InterestDto result = interestService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> update(
            @PathVariable UUID interestId,
            @Valid @RequestBody InterestUpdateRequest request
    ) {
        InterestDto result = interestService.update(interestId, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> delete(@PathVariable UUID interestId) {
        interestService.delete(interestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public CursorPageResponse<InterestDto> getInterests(
            @RequestParam(required = false) String keyword,
            @RequestParam String orderBy,
            @RequestParam Sort.Direction direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
            @RequestParam int limit,
            @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId
    ) {
        InterestSearchCondition condition = new InterestSearchCondition(
                keyword, parseSortType(orderBy), direction, cursor, after, limit
        );
        return interestService.getInterests(condition, userId);
    }

    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionDto> subscribe(
            @PathVariable UUID interestId,
            @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId
    ) {
        SubscriptionDto result = interestService.subscribe(userId, interestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @PathVariable UUID interestId,
            @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId
    ) {
        interestService.unsubscribe(userId, interestId);
        return ResponseEntity.ok().build();
    }

    // orderBy 파라미터 파싱 실패는 요청 형식 문제이므로 400으로 변환합니다.
    // 매핑 규칙 자체(InterestSortType.from)는 컨트롤러가 아닌 enum이 책임집니다.
    private InterestSortType parseSortType(String orderBy) {
        try {
            return InterestSortType.from(orderBy);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
