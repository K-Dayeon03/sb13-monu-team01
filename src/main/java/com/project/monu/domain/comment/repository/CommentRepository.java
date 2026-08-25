package com.project.monu.domain.comment.repository;

import com.project.monu.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {


    // 삭제되지 않은 댓글 전체 조회
    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.article.id = :articleId
          AND c.deletedAt IS NULL
        """)
    List<Comment> findAllActiveByArticleId(@Param("articleId") UUID articleId);

    // 삭제되지 않은 댓글 단건 조회
    @Query("""
    SELECT c
    FROM Comment c
    WHERE c.id = :commentId
      AND c.deletedAt IS NULL
    """)
    Optional<Comment> findActiveById(@Param("commentId") UUID commentId);

    @Query("""
        SELECT c.id
        FROM Comment c
        WHERE c.article.id = :articleId
    """)
    List<UUID> findIdsByArticleId(@Param("articleId") UUID articleId);

    void deleteAllByArticle_Id(UUID articleId);
}
