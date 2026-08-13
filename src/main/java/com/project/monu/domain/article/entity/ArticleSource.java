package com.project.monu.domain.article.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Table(
        name = "article_sources",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_article_sources_name", columnNames = "name")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleSource {


    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;


    // 출처 이름 (예: NAVER, 한국경제, 조선일보...)
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    // 출처 유형 (API / RSS)
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private SourceType type;

    // 수집 URL (API 엔드포인트 또는 RSS 피드 주소)
    @Column(name = "source_url", nullable = false, length = 2048)
    private String sourceUrl;

    // 수집사용여부
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


    @Builder
    public ArticleSource(String name, SourceType type, String sourceUrl) {
        this.name = name;
        this.type = type;
        this.sourceUrl = sourceUrl;
        this.enabled = true;
    }

    public void enable() {
        this.enabled = true;
    }
    public void disable() {
        this.enabled = false;
    }

    public void updateSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}