package com.project.monu.domain.useractivity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.SourceType;
import com.project.monu.domain.comment.entity.Comment;
import com.project.monu.domain.comment.entity.CommentLike;
import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
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
        JpaUserActivityCommentLikeRepository.class
})
class JpaUserActivityCommentLikeRepositoryTest {

    private static final int RECENT_ACTIVITY_LIMIT = 10;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserActivityCommentLikeRepository commentLikeRepository;

    @Test
    void 좋아요한_댓글은_최대_10개만_조회한다() {
        User likedBy = user("liked-by@email.com", "좋아요사용자");
        User commentWriter = user("writer@email.com", "댓글작성자");
        ArticleSource source = source("NAVER");

        IntStream.rangeClosed(1, 11)
                .forEach(index -> {
                    Article article = article(source, "기사 제목 " + index);
                    Comment comment = comment(article, commentWriter, "좋아요한 댓글 " + index);
                    entityManager.persist(new CommentLike(comment, likedBy));
                });

        flushAndClear();

        List<UserActivityCommentLikeResponse> commentLikes =
                commentLikeRepository.findAllByUserId(likedBy.getId());

        assertThat(commentLikes).hasSize(10);
    }

    @Test
    void 다른_사용자가_좋아요한_댓글은_조회하지_않는다() {
        User requestUser = user("request@email.com", "요청사용자");
        User otherUser = user("other@email.com", "다른사용자");
        User commentWriter = user("writer@email.com", "댓글작성자");

        ArticleSource source = source("NAVER");
        Article article = article(source, "기사 제목");
        Comment comment = comment(article, commentWriter, "다른 사용자가 좋아요한 댓글");

        entityManager.persist(new CommentLike(comment, otherUser));

        flushAndClear();

        List<UserActivityCommentLikeResponse> commentLikes =
                commentLikeRepository.findAllByUserId(requestUser.getId());

        assertThat(commentLikes).isEmpty();
    }

    @Test
    void 삭제된_댓글에_대한_좋아요는_조회하지_않는다() {
        User likedBy = user("liked-by@email.com", "좋아요사용자");
        User commentWriter = user("writer@email.com", "댓글작성자");

        ArticleSource source = source("NAVER");
        Article article = article(source, "기사 제목");
        Comment comment = comment(article, commentWriter, "삭제된 댓글");
        comment.delete();

        entityManager.persist(new CommentLike(comment, likedBy));

        flushAndClear();

        List<UserActivityCommentLikeResponse> commentLikes =
                commentLikeRepository.findAllByUserId(likedBy.getId());

        assertThat(commentLikes).isEmpty();
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