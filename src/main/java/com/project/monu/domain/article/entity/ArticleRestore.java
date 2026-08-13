package com.project.monu.domain.article.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "article_restores")
public class ArticleRestore {

    // 복구 이력 식별자
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 복구 대상 날짜
    @Column(name = "restore_date", nullable = false)
    private LocalDate restoreDate;

    // 새로 복구된 기사 수
    @Column(name = "restored_count", nullable = false)
    private Long restoredCount = 0L;

    // 복구 이력 생성 시각
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 복구에 사용된 백업 이력
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backup_id", nullable = false)
    private ArticleBackup backup;
}