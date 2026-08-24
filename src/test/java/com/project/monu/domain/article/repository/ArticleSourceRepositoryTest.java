package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
class ArticleSourceRepositoryTest {

    @Autowired
    private   ArticleSourceRepository articleSourceRepository;

    @Test
    void 활성화된_출처만_조회한다() {
        // given
        ArticleSource articleSource = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://naver.example.com")
                .build();

        ArticleSource inactiveSource = ArticleSource.builder()
                .name("DISABLED_RSS")
                .type(SourceType.RSS)
                .sourceUrl("https://disabled.example.com/rss")
                .build();

        inactiveSource.disable();

        articleSourceRepository.saveAll(
                List.of(articleSource, inactiveSource)
        );

        // when
        List<ArticleSource> result =
                articleSourceRepository.findAllByEnabledTrue();

        // then
        assertThat(result)
                .extracting(ArticleSource::getName)
                .containsExactly("NAVER");
    }

}