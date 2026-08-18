package com.project.monu.domain.article.collector.naver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record NaverNewsResponse(
        int total,
        int start,
        int display,
        List<Item> items



) {
    public record Item(
            String title,
            @JsonProperty("originallink") String originalLink,
            String link,
            String description,
            @JsonProperty("pubDate") String pubDate
    ) {}
}
