package com.project.monu.domain.notification.dto;

import java.time.Instant;
import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        String nextCursor,
        Instant nextAfter,
        int limit,
        boolean hasNext
) {
}