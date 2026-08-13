package com.project.monu.domain.article.entity;

import jakarta.persistence.*;
import lombok.Getter;
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
public class Article {

    // 기사 식별자
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;
//
//    // 기사 출처
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 50)
//    private ArticleSource source;

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
}