package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.entity.Comment;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserActivityCommentRepository implements UserActivityCommentRepository {

    private final EntityManager entityManager;
    private static final int RECENT_ACTIVITY_LIMIT = 10;

    public JpaUserActivityCommentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<CommentDto> findAllByUserId(UUID userId) {
        List<Comment> comments = findComments(userId);

        if (comments.isEmpty()) {
            return List.of();
        }

        List<UUID> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        Map<UUID, Long> likeCounts = countLikesByCommentId(commentIds);
        List<UUID> likedCommentIds = findLikedCommentIds(userId, commentIds);

        return comments.stream()
                .map(comment -> toCommentDto(comment, likeCounts, likedCommentIds))
                .toList();
    }

    private List<Comment> findComments(UUID userId) {
        return entityManager.createQuery("""
                        select comment
                        from Comment comment
                        join fetch comment.article article
                        join fetch comment.user user
                        where user.id = :userId
                          and comment.deletedAt is null
                        order by comment.createdAt desc
                        """, Comment.class)
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

    private List<UUID> findLikedCommentIds(
            UUID userId,
            List<UUID> commentIds
    ) {
        return entityManager.createQuery("""
                        select commentLike.comment.id
                        from CommentLike commentLike
                        where commentLike.likedBy.id = :userId
                          and commentLike.comment.id in :commentIds
                        """, UUID.class)
                .setParameter("userId", userId)
                .setParameter("commentIds", commentIds)
                .getResultList();
    }

    private CommentDto toCommentDto(
            Comment comment,
            Map<UUID, Long> likeCounts,
            List<UUID> likedCommentIds
    ) {
        UUID commentId = comment.getId();

        return new CommentDto(
                commentId,
                comment.getArticle().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                likeCounts.getOrDefault(commentId, 0L),
                likedCommentIds.contains(commentId),
                comment.getCreatedAt()
        );
    }
}