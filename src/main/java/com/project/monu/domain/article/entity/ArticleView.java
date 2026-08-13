package com.project.monu.domain.article.entity;


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

    /**
     * 조회한 사용자
     * user 엔티티 병합 후 @ManyToOne(User) 로 교체 필요
     */
    @Column(name = "viewer_id", columnDefinition = "uuid", nullable = false)
    private UUID viewerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Builder
    public ArticleView(UUID viewerId, Article article) {
        this.viewerId = viewerId;
        this.article = article;
    }
}
