package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import java.util.List;
import java.util.UUID;

public interface UserActivitySubscriptionRepository {

    List<SubscriptionDto> findAllByUserId(UUID userId);
}