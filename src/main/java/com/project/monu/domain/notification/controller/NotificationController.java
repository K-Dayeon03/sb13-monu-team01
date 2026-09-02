package com.project.monu.domain.notification.controller;

import com.project.monu.domain.notification.dto.NotificationConfirmAllResponse;
import com.project.monu.domain.notification.dto.NotificationResponse;
import com.project.monu.domain.notification.service.NotificationService;
import com.project.monu.global.constant.RequestHeaders;
import com.project.monu.global.dto.CursorPageResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public CursorPageResponse<NotificationResponse> getNotifications(
            @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID requestUserId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return notificationService.getNotifications(requestUserId, limit);
    }

    @PatchMapping("/{notificationId}")
    public NotificationResponse confirmNotification(
            @PathVariable UUID notificationId,
            @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID requestUserId
    ) {
        return notificationService.confirmNotification(notificationId, requestUserId);
    }

    @PatchMapping
    public NotificationConfirmAllResponse confirmAllNotifications(
            @RequestHeader(RequestHeaders.REQUEST_USER_ID) UUID requestUserId
    ) {
        return notificationService.confirmAllNotifications(requestUserId);
    }
}
