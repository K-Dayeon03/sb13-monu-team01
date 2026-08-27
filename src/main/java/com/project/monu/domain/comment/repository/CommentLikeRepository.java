package com.project.monu.domain.comment.repository;

import com.project.monu.domain.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    void deleteAllByComment_IdIn(List<UUID> commentIds);

    long countByComment_Id(UUID commentId);
}

