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
import com.project.monu.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "관심사", description = "관심사 등록/수정/삭제/조회와 구독 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interests")
public class InterestController {

    private final InterestService interestService;

    @Operation(
            summary = "관심사 등록",
            description = "새 관심사를 이름과 키워드 목록으로 등록합니다. "
                    + "기존 관심사 중 이름이 유사한 것이 있으면 등록이 거부됩니다 "
                    + "(이름 길이가 4자 이하면 편집 거리 1 이내, 5자 이상이면 유사도 80% 이상을 유사한 것으로 판단)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음 (이름 공백/50자 초과, 키워드 0개 또는 10개 초과 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 유사한 이름의 관심사가 존재함",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<InterestDto> register(@Valid @RequestBody InterestRegisterRequest request) {
        InterestDto result = interestService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
            summary = "관심사 키워드 수정",
            description = "관심사의 키워드 목록을 수정합니다. 이름은 등록 이후 변경할 수 없습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값이 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "관심사를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{interestId}")
    public ResponseEntity<InterestDto> update(
            @Parameter(description = "수정할 관심사 ID") @PathVariable UUID interestId,
            @Valid @RequestBody InterestUpdateRequest request
    ) {
        InterestDto result = interestService.update(interestId, request);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "관심사 삭제",
            description = "관심사를 삭제합니다. 연관된 구독, 기사 연결 정보도 함께 정리됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "관심사를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> delete(@Parameter(description = "삭제할 관심사 ID") @PathVariable UUID interestId) {
        interestService.delete(interestId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "관심사 목록 조회",
            description = "커서 기반 페이지네이션으로 관심사 목록을 조회합니다. "
                    + "keyword로 이름을 부분 검색할 수 있고, orderBy/direction으로 정렬 기준을 지정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "orderBy 값이 유효하지 않거나 커서 형식이 잘못됨",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public CursorPageResponse<InterestDto> getInterests(
            @Parameter(description = "이름 부분 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "정렬 기준 (예: subscriberCount, name)") @RequestParam String orderBy,
            @Parameter(description = "정렬 방향 (ASC/DESC)") @RequestParam Sort.Direction direction,
            @Parameter(description = "다음 페이지 커서 (\"정렬값_id\" 형식)") @RequestParam(required = false) String cursor,
            @Parameter(description = "커서와 함께 사용하는 기준 시각") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
            @Parameter(description = "페이지 크기 (기본 10, 최대 100)") @RequestParam int limit,
            @Parameter(description = "요청 사용자 ID") @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId
    ) {
        InterestSearchCondition condition = new InterestSearchCondition(
                keyword, parseSortType(orderBy), direction, cursor, after, limit
        );
        return interestService.getInterests(condition, userId);
    }

    @Operation(
            summary = "관심사 구독",
            description = "지정한 관심사를 구독합니다. 이미 구독 중이면 409를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "구독 성공"),
            @ApiResponse(responseCode = "404", description = "관심사를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 구독 중이거나(SUBSCRIPTION_DUPLICATION), "
                    + "동시 요청으로 구독자 수 갱신이 충돌함(INTEREST_CONCURRENT_UPDATE)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{interestId}/subscriptions")
    public ResponseEntity<SubscriptionDto> subscribe(
            @Parameter(description = "구독할 관심사 ID") @PathVariable UUID interestId,
            @Parameter(description = "요청 사용자 ID") @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId
    ) {
        SubscriptionDto result = interestService.subscribe(userId, interestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(
            summary = "관심사 구독 취소",
            description = "지정한 관심사 구독을 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구독 취소 성공"),
            @ApiResponse(responseCode = "404", description = "구독 내역을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "동시 요청으로 구독자 수 갱신이 충돌함(INTEREST_CONCURRENT_UPDATE)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{interestId}/subscriptions")
    public ResponseEntity<Void> unsubscribe(
            @Parameter(description = "구독 취소할 관심사 ID") @PathVariable UUID interestId,
            @Parameter(description = "요청 사용자 ID") @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID userId
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
