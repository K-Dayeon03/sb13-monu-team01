package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.comment.entity.Comment;
import com.project.monu.domain.comment.entity.CommentLike;
import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserActivityCommentLikeRepository implements UserActivityCommentLikeRepository {

    private final EntityManager entityManager;
    private static final int RECENT_ACTIVITY_LIMIT = 10;

    public JpaUserActivityCommentLikeRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<UserActivityCommentLikeResponse> findAllByUserId(UUID userId) {
        List<CommentLike> commentLikes = findCommentLikes(userId);

        if (commentLikes.isEmpty()) {
            return List.of();
        }

        List<UUID> commentIds = commentLikes.stream()
                .map(commentLike -> commentLike.getComment().getId())
                .toList();

        Map<UUID, Long> likeCounts = countLikesByCommentId(commentIds);

        return commentLikes.stream()
                .map(commentLike -> toResponse(commentLike, likeCounts))
                .toList();
    }

    private List<CommentLike> findCommentLikes(UUID userId) {
        return entityManager.createQuery("""
                        select commentLike
                        from CommentLike commentLike
                        join fetch commentLike.comment comment
                        join fetch comment.article article
                        join fetch comment.user commentUser
                        join fetch commentLike.likedBy likedBy
                        where likedBy.id = :userId
                          and comment.deletedAt is null
                        order by commentLike.createdAt desc
                        """, CommentLike.class)
                .setParameter("userId", userId)
                .setMaxResults(RECENT_ACTIVITY_LIMIT)
                .getResultList();
    }

    private Map<UUID, Long> countLikesByCommentId(List<UUID> commentIds) {
        List<Object[]> rows = entityManager.createQuery("""
                        select commentLike.comment.id, count(commentLike.id)
                        from CommentLike commentLike
                        where commentLike.comment.id in :commentIds
                        group by commentLike.comment.id
                        """, Object[].class)
                .setParameter("commentIds", commentIds)
                .getResultList();

        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));
    }

    private UserActivityCommentLikeResponse toResponse(
            CommentLike commentLike,
            Map<UUID, Long> likeCounts
    ) {
        Comment comment = commentLike.getComment();
        UUID commentId = comment.getId();

        return new UserActivityCommentLikeResponse(
                commentLike.getId(),
                commentLike.getLikedBy().getId(),
                commentLike.getCreatedAt(),
                commentId,
                comment.getArticle().getId(),
                comment.getArticle().getTitle(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                likeCounts.getOrDefault(commentId, 0L),
                comment.getCreatedAt()
        );
    }
}