package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.ArticleView;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.users.entity.User;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
class ArticleViewRepositoryTest {

    // 실제 User, Article, ArticleView를 DB에 저장한 뒤 Repository 메서드 결과를 검증
    @Autowired
    private ArticleViewRepository articleViewRepository;

    @Autowired
    private EntityManager em;

    @Test
    void 현재_사용자가_조회한_기사_ID만_반환한다() {
        // given
        User userA = user("userA@test.com", "userA");
        User userB = user("userB@test.com", "userB");

        ArticleSource source = source("NAVER");

        Article article1 = article(source, "article1");
        Article article2 = article(source, "article2");

        articleView(userA, article1);
        articleView(userB, article2);

        flushAndClear();

        // when
        Set<UUID> result = articleViewRepository.findViewedArticleIds(
                userA.getId(),
                List.of(article1.getId(), article2.getId())
        );

        // then
        assertThat(result).containsExactly(article1.getId());
        assertThat(result).doesNotContain(article2.getId());
    }

    @Test
    void 사용자의_기사_조회_이력_존재_여부를_확인한다() {
        // given
        User user = user("viewer@test.com", "viewer");
        ArticleSource source = source("HANKYUNG");
        Article viewedArticle = article(source, "viewed-article");
        Article notViewedArticle = article(source, "not-viewed-article");

        articleView(user, viewedArticle);

        flushAndClear();

        // when
        boolean viewed = articleViewRepository.existsByViewerIdAndArticleId(
                user.getId(),
                viewedArticle.getId()
        );
        boolean notViewed = articleViewRepository.existsByViewerIdAndArticleId(
                user.getId(),
                notViewedArticle.getId()
        );

        // then
        assertThat(viewed).isTrue();
        assertThat(notViewed).isFalse();
    }

    private User user(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password("encoded-password")
                .build();

        em.persist(user);
        return user;
    }

    private ArticleSource source(String name) {
        ArticleSource source = ArticleSource.builder()
                .name(name)
                .type(SourceType.RSS)
                .sourceUrl("https://example.com/" + name)
                .build();

        em.persist(source);
        return source;
    }

    private Article article(ArticleSource source, String title) {
        Article article = Article.builder()
                .source(source)
                .sourceUrl("https://example.com/articles/" + title)
                .title(title)
                .publishDate(Instant.parse("2026-08-18T00:00:00Z"))
                .summary("summary")
                .build();

        em.persist(article);
        return article;
    }

    private ArticleView articleView(User user, Article article) {
        ArticleView articleView = ArticleView.builder()
                .viewer(user)
                .article(article)
                .build();

        em.persist(articleView);
        return articleView;
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}
