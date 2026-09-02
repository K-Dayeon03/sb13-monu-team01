package com.project.monu.domain.article.entity;


import com.project.monu.domain.users.entity.User;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(
        name = "article_views",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_article_views_viewer_article",
                        columnNames = {"viewer_id", "article_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleView {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id", nullable = false)
    private User viewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Builder
    public ArticleView(User viewer, Article article) {
        this.viewer = viewer;
        this.article = article;
    }
}
