package com.project.monu.domain.article.dto.response;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ArticleDtoTest {

    @Test
    void 기사_출처의_표시_이름을_반환한다() {
        ArticleSource source = ArticleSource.builder()
                .name("NAVER")
                .displayName("네이버")
                .type(SourceType.API)
                .sourceUrl("https://openapi.naver.com/v1/search/news.json")
                .build();

        // 프로젝트의 Article 생성 방식에 맞춰 작성
        Article article = Article.builder()
                .source(source)
                // Article 생성에 필요한 나머지 필드
                .build();

        ArticleDto result = ArticleDto.from(article, false);

        assertThat(result.source()).isEqualTo("네이버");

    }
}