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

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserActivityCommentLikeRepository commentLikeRepository;

    @Test
    void 사용자가_좋아요한_댓글_목록을_조회한다() {
        User likedBy = user("liked-by@email.com", "좋아요사용자");
        User commentWriter = user("writer@email.com", "댓글작성자");
        User otherLikeUser = user("other-like@email.com", "다른좋아요사용자");

        ArticleSource source = source("NAVER");
        Article article = article(source, "기사 제목");

        Comment comment = comment(article, commentWriter, "좋아요한 댓글");

        CommentLike myCommentLike = new CommentLike(comment, likedBy);
        CommentLike otherCommentLike = new CommentLike(comment, otherLikeUser);

        entityManager.persist(myCommentLike);
        entityManager.persist(otherCommentLike);

        flushAndClear();

        List<UserActivityCommentLikeResponse> commentLikes =
                commentLikeRepository.findAllByUserId(likedBy.getId());

        assertThat(commentLikes).hasSize(1);

        UserActivityCommentLikeResponse result = commentLikes.get(0);

        assertThat(result.id()).isEqualTo(myCommentLike.getId());
        assertThat(result.likedBy()).isEqualTo(likedBy.getId());
        assertThat(result.createdAt()).isNotNull();
        assertThat(result.commentId()).isEqualTo(comment.getId());
        assertThat(result.articleId()).isEqualTo(article.getId());
        assertThat(result.articleTitle()).isEqualTo("기사 제목");
        assertThat(result.commentUserId()).isEqualTo(commentWriter.getId());
        assertThat(result.commentUserNickname()).isEqualTo("댓글작성자");
        assertThat(result.commentContent()).isEqualTo("좋아요한 댓글");
        assertThat(result.commentLikeCount()).isEqualTo(2L);
        assertThat(result.commentCreatedAt()).isNotNull();
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