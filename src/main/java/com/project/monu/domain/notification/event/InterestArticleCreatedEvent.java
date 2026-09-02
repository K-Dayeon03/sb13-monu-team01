package com.project.monu.domain.notification.event;

import java.util.List;
import java.util.UUID;

public record InterestArticleCreatedEvent(
        UUID interestId,
        String interestName,
        int articleCount,
        List<UUID> subscriberUserIds
) {
}