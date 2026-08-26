package com.project.monu.domain.article.collector.naver;

import com.project.monu.domain.article.collector.dto.CollectedArticle;
import com.project.monu.domain.article.collector.exception.ArticleMappingException;
import com.project.monu.domain.article.collector.naver.dto.NaverNewsResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class NaverArticleMapper {

    public CollectedArticle toCollectedArticle(NaverNewsResponse.Item item) {
        return new CollectedArticle(
                cleanText(item.title()),
                item.originalLink(),
                cleanText(item.description()),
                parsePublishedAt(item.pubDate())
        );
    }

    private String cleanText(String text) {
        String withoutTags = text.replaceAll("<[^>]*>", ""); // 1. HTML 태그 제거
        return HtmlUtils.htmlUnescape(withoutTags); // 2. HTML 엔티티 디코딩
    }

    private Instant parsePublishedAt(String pubDate){
        try {
            return OffsetDateTime
                    .parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant();
        }catch (DateTimeParseException e){
            throw new ArticleMappingException("잘못된 pubDate 포맷: " + pubDate, e);
        }
    }
}
