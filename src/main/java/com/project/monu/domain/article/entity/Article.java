package com.project.monu.domain.article.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "article",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_article_source_url", columnNames = "source_url")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

    // 기사 식별자
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 기사 출처
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private ArticleSource source;

    // 원본 기사 링크
    @Column(name = "source_url", length = 2048, nullable = false)
    private String sourceUrl;

    // 기사 제목
    @Column(length = 500, nullable = false)
    private String title;

    // 기사 발행 시각
    @Column(name = "publish_date", nullable = false)
    private Instant publishDate;

    // 기사 요약
    @Column(columnDefinition = "text")
    private String summary;

    // 기사 통계 정보
    @Column(name = "comment_count", nullable = false)
    private Long commentCount = 0L;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    // 생성/수정/삭제 시각
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Builder
    public Article(ArticleSource source, String sourceUrl, String title,
                   Instant publishDate, String summary) {
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.publishDate = publishDate;
        this.summary = summary;
        this.commentCount = 0L;
        this.viewCount = 0L;
    }

    //논리 삭제: 삭제 시각 기록
    public void softDelete() {
        this.deletedAt = Instant.now();
    }
    // 삭제 여부 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

}