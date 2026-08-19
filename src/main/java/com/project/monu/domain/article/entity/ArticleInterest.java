package com.project.monu.domain.article.entity;


import com.project.monu.domain.interest.entity.Interest;
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
        name = "article_interests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_article_interests_article_interest",
                        columnNames = {"article_id", "interest_id"}
                )

        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleInterest {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 매칭된 기사
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id",  nullable = false)
    private Article article;

    // interestId(UUID) → interest(@ManyToOne)로 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public ArticleInterest(Article article, Interest interest) {
        this.article = article;
        this.interest = interest;
    }
}
