package com.project.monu.domain.article.collector.naver.dto;

import java.util.List;
import java.util.UUID;

public record InterestKeywords(
        UUID interestId,
        List<String> keywords
) {
}
