package com.project.monu.domain.useractivity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.ArticleView;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.users.entity.User;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        JpaUserActivityArticleViewRepository.class
})
class JpaUserActivityArticleViewRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserActivityArticleViewRepository articleViewRepository;

    @Test
    void 사용자가_조회한_기사_목록을_조회한다() {
        User user = user("viewer@email.com", "조회사용자");
        ArticleSource source = source("NAVER");
        Article article = article(source, "조회한 기사 제목");

        ArticleView articleView = articleView(user, article);

        flushAndClear();

        List<ArticleViewDto> articleViews = articleViewRepository.findAllByUserId(user.getId());

        assertThat(articleViews).hasSize(1);

        ArticleViewDto result = articleViews.get(0);

        assertThat(result.id()).isEqualTo(articleView.getId());
        assertThat(result.viewedBy()).isEqualTo(user.getId());
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.articleId()).isEqualTo(article.getId());
        assertThat(result.source()).isEqualTo("NAVER");
        assertThat(result.sourceUrl()).isEqualTo("https://example.com/articles/조회한 기사 제목");
        assertThat(result.articleTitle()).isEqualTo("조회한 기사 제목");
        assertThat(result.articlePublishedDate()).isEqualTo(Instant.parse("2026-08-26T00:00:00Z"));
        assertThat(result.articleSummary()).isEqualTo("기사 요약");
        assertThat(result.articleCommentCount()).isEqualTo(0L);
        assertThat(result.articleViewCount()).isEqualTo(0L);
    }

    @Test
    void 다른_사용자가_조회한_기사는_조회하지_않는다() {
        User requestUser = user("request@email.com", "요청사용자");
        User otherUser = user("other@email.com", "다른사용자");
        ArticleSource source = source("NAVER");
        Article article = article(source, "다른 사용자가 조회한 기사");

        articleView(otherUser, article);

        flushAndClear();

        List<ArticleViewDto> articleViews =
                articleViewRepository.findAllByUserId(requestUser.getId());

        assertThat(articleViews).isEmpty();
    }

    @Test
    void 삭제된_기사의_조회_이력은_조회하지_않는다() {
        User user = user("viewer@email.com", "조회사용자");
        ArticleSource source = source("NAVER");
        Article article = article(source, "삭제된 기사");

        articleView(user, article);
        article.softDelete();

        flushAndClear();

        List<ArticleViewDto> articleViews = articleViewRepository.findAllByUserId(user.getId());

        assertThat(articleViews).isEmpty();
    }

    private User user(String email, String nickname) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password("encoded-password")
                .build();

        entityManager.persist(user);
        return user;
    }

    private ArticleSource source(String name) {
        ArticleSource source = ArticleSource.builder()
                .name(name)
                .type(SourceType.RSS)
                .sourceUrl("https://example.com/" + name)
                .build();

        entityManager.persist(source);
        return source;
    }

    private Article article(ArticleSource source, String title) {
        Article article = Article.builder()
                .source(source)
                .sourceUrl("https://example.com/articles/" + title)
                .title(title)
                .publishDate(Instant.parse("2026-08-26T00:00:00Z"))
                .summary("기사 요약")
                .build();

        entityManager.persist(article);
        return article;
    }

    private ArticleView articleView(User user, Article article) {
        ArticleView articleView = ArticleView.builder()
                .viewer(user)
                .article(article)
                .build();

        entityManager.persist(articleView);
        return articleView;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}