package com.project.monu.domain.article.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleSourceTest {

    @Test
    void 출처를_생성하면_기본적으로_활성화된다() {
        // given & when
        // 새로 등록한 출처는 수집 대상이므로 enabled=true가 기본값입니다.
        ArticleSource source = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://openapi.naver.com")
                .build();

        // then
        assertThat(source.getName()).isEqualTo("NAVER");
        assertThat(source.getType()).isEqualTo(SourceType.API);
        assertThat(source.getSourceUrl()).isEqualTo("https://openapi.naver.com");
        assertThat(source.isEnabled()).isTrue();
    }

    @Test
    void 출처를_비활성화하고_다시_활성화할_수_있다() {
        // given
        ArticleSource source = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://openapi.naver.com")
                .build();

        // when
        // 수집을 잠시 멈춰야 할 때는 비활성화하고, 다시 수집할 때 활성화합니다.
        source.disable();

        // then
        assertThat(source.isEnabled()).isFalse();

        // when
        source.enable();

        // then
        assertThat(source.isEnabled()).isTrue();
    }

    @Test
    void 수집_URL을_수정할_수_있다() {
        // given
        ArticleSource source = ArticleSource.builder()
                .name("NAVER")
                .type(SourceType.API)
                .sourceUrl("https://old.example.com")
                .build();

        // when
        source.updateSourceUrl("https://new.example.com");

        // then
        assertThat(source.getSourceUrl()).isEqualTo("https://new.example.com");
    }
}
