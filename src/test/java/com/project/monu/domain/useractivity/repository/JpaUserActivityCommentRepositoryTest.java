package com.project.monu.domain.useractivity.repository;


import static org.assertj.core.api.Assertions.assertThat;


import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.entity.Comment;
import com.project.monu.domain.comment.entity.CommentLike;
import com.project.monu.domain.users.entity.User;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        JpaUserActivityCommentRepository.class
})
class JpaUserActivityCommentRepositoryTest {

    private static final int RECENT_ACTIVITY_LIMIT = 10;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserActivityCommentRepository commentRepository;


@Test
void 작성한_댓글은_최대_10개만_조회한다() {
    User user = user("user@email.com", "사용자");
    ArticleSource source = source("NAVER");
    Article article = article(source, "기사 제목");

    IntStream.rangeClosed(1, 11)
            .forEach(index -> comment(article, user, "댓글 " + index));

    flushAndClear();

    List<CommentDto> comments = commentRepository.findAllByUserId(user.getId());

    assertThat(comments).hasSize(10);
}

    @Test
    void 내가_좋아요한_내_댓글은_likedByMe가_true다() {
        User user = user("user@email.com", "사용자");
        ArticleSource source = source("NAVER");
        Article article = article(source, "기사 제목");

        Comment comment = comment(article, user, "내가 작성한 댓글");
        entityManager.persist(new CommentLike(comment, user));

        flushAndClear();

        List<CommentDto> comments = commentRepository.findAllByUserId(user.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).likedByMe()).isTrue();
        assertThat(comments.get(0).likeCount()).isEqualTo(1L);
    }

    @Test
    void 다른_사용자가_작성한_댓글은_조회하지_않는다() {
        User requestUser = user("request@email.com", "요청사용자");
        User otherUser = user("other@email.com", "다른사용자");
        ArticleSource source = source("NAVER");
        Article article = article(source, "기사 제목");

        comment(article, otherUser, "다른 사용자가 작성한 댓글");

        flushAndClear();

        List<CommentDto> comments = commentRepository.findAllByUserId(requestUser.getId());

        assertThat(comments).isEmpty();
    }

    @Test
    void 삭제된_댓글은_조회하지_않는다() {
        User user = user("user@email.com", "사용자");
        ArticleSource source = source("NAVER");
        Article article = article(source, "기사 제목");

        Comment comment = comment(article, user, "삭제된 댓글");
        comment.delete();

        flushAndClear();

        List<CommentDto> comments = commentRepository.findAllByUserId(user.getId());

        assertThat(comments).isEmpty();
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

    private Comment comment(
            Article article,
            User user,
            String content
    ) {
        Comment comment = new Comment(article, user, content);
        entityManager.persist(comment);
        return comment;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}