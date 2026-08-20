package com.project.monu.domain.interest.controller;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.service.InterestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.monu.global.dto.CursorPageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping
    public CursorPageResponse<InterestDto> getInterests(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "SUBSCRIBER_COUNT") InterestSortType sortType,
            @RequestParam(required = false) String nextCursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        InterestSearchCondition condition = new InterestSearchCondition(keyword, sortType, nextCursor, size);
        return interestService.getInterests(condition);
    }

}
