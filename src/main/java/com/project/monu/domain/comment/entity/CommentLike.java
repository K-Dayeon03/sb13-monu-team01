package com.project.monu.domain.comment.entity;

import com.project.monu.domain.users.entity.User;
import com.project.monu.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "comment_like", uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_like_comment_user",
                columnNames = {"comment_id", "liked_by"})
        })
public class CommentLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liked_by", nullable = false)
    private User likedBy;

    public CommentLike(Comment comment, User likedBy) {
        this.comment = comment;
        this.likedBy = likedBy;
    }
}