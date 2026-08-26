package com.project.monu.domain.useractivity.controller;

import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import com.project.monu.domain.useractivity.service.UserActivityService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-activities")
public class UserActivityController {

    private final UserActivityService userActivityService;

    public UserActivityController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @GetMapping("/{userId}")
    public UserActivityResponse getUserActivity(@PathVariable UUID userId) {
        return userActivityService.getUserActivity(userId);
    }
}