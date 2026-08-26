package com.project.monu.domain.notification.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.monu.domain.notification.dto.NotificationConfirmAllResponse;
import com.project.monu.domain.notification.dto.NotificationResponse;
import com.project.monu.domain.notification.service.NotificationService;
import com.project.monu.global.dto.CursorPageResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    private static final String REQUEST_USER_ID_HEADER = "MoNew-Request-User-ID";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void 미확인_알림_목록을_조회한다() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        NotificationResponse notification = new NotificationResponse(
                notificationId,
                Instant.parse("2026-08-24T00:00:00Z"),
                null,
                userId,
                "새로운 알림입니다.",
                "comment",
                resourceId,
                false
        );

        CursorPageResponse<NotificationResponse> response = CursorPageResponse.of(
                List.of(notification),
                null,
                null,
                10,
                1L,
                false
        );

        when(notificationService.getNotifications(userId, 10))
                .thenReturn(response);

        mockMvc.perform(get("/api/notifications")
                        .header(REQUEST_USER_ID_HEADER, userId.toString())
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(notificationId.toString()))
                .andExpect(jsonPath("$.content[0].content").value("새로운 알림입니다."))
                .andExpect(jsonPath("$.content[0].resourceType").value("comment"))
                .andExpect(jsonPath("$.content[0].resourceId").value(resourceId.toString()))
                .andExpect(jsonPath("$.content[0].confirmed").value(false))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(notificationService).getNotifications(userId, 10);
    }

    @Test
    void limit이_없으면_기본값_10으로_미확인_알림_목록을_조회한다() throws Exception {
        UUID userId = UUID.randomUUID();

        when(notificationService.getNotifications(userId, 10))
                .thenReturn(CursorPageResponse.of(List.of(), null, null, 10, 0L, false));

        mockMvc.perform(get("/api/notifications")
                        .header(REQUEST_USER_ID_HEADER, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(10));

        verify(notificationService).getNotifications(userId, 10);
    }

    @Test
    void 알림을_단건_확인한다() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        NotificationResponse response = new NotificationResponse(
                notificationId,
                Instant.parse("2026-08-24T00:00:00Z"),
                Instant.parse("2026-08-24T01:00:00Z"),
                userId,
                "새로운 알림입니다.",
                "comment",
                resourceId,
                true
        );

        when(notificationService.confirmNotification(notificationId, userId))
                .thenReturn(response);

        mockMvc.perform(patch("/api/notifications/{notificationId}", notificationId)
                        .header(REQUEST_USER_ID_HEADER, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.confirmed").value(true));

        verify(notificationService).confirmNotification(notificationId, userId);
    }

    @Test
    void 사용자의_미확인_알림을_전체_확인한다() throws Exception {
        UUID userId = UUID.randomUUID();

        when(notificationService.confirmAllNotifications(userId))
                .thenReturn(new NotificationConfirmAllResponse(3));

        mockMvc.perform(patch("/api/notifications")
                        .header(REQUEST_USER_ID_HEADER, userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedCount").value(3));

        verify(notificationService).confirmAllNotifications(userId);
    }

    @Test
    void 사용자_ID_헤더가_없으면_400을_응답한다() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isBadRequest());
    }
}
