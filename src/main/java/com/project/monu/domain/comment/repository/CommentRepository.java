package com.project.monu.domain.comment.repository;

import com.project.monu.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.article.id = :articleId
          AND c.deletedAt IS NULL
        """)
    List<Comment> findAllActiveByArticleId(
            @Param("articleId") UUID articleId
    );
}