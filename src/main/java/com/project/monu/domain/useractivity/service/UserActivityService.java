package com.project.monu.domain.useractivity.service;

import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import java.util.UUID;

public interface UserActivityService {

    UserActivityResponse getUserActivity(UUID userId);
}