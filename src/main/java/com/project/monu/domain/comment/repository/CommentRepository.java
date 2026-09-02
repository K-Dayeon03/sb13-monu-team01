package com.project.monu.domain.comment.repository;

import com.project.monu.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository
        extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {

    @Query("""
        SELECT c.id
        FROM Comment c
        WHERE c.article.id = :articleId
        """)

    List<UUID> findIdsByArticleId(@Param("articleId") UUID articleId);

    List<Comment> findAllByUser_Id(UUID userId);

    void deleteAllByArticle_Id(UUID articleId);
}