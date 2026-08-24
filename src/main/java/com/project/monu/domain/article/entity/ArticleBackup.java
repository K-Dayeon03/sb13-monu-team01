package com.project.monu.domain.article.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "article_backups",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_article_backup_s3_key", columnNames = "s3_key")
        }
)
@Getter
public class ArticleBackup {

    // 백업 이력 식별자
    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    // 백업 대상 날짜
    @Column(name = "backup_date", nullable = false)
    private LocalDate backupDate;

    // 백업 파일이 저장된 S3 위치 정보
    @Column(name = "s3_bucket", nullable = false, length = 63)
    private String s3Bucket;

    @Column(name = "s3_key", nullable = false, length = 2048)
    private String s3Key;

    // 백업된 기사 수
    @Column(name = "article_count", nullable = false)
    private Long articleCount = 0L;

    // 백업 이력 생성 시각
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ArticleBackup() {
    }

    private ArticleBackup(LocalDate backupDate, String s3Bucket, String s3Key, Long articleCount) {
        this.backupDate = backupDate;
        this.s3Bucket = s3Bucket;
        this.s3Key = s3Key;
        this.articleCount = articleCount;
    }

    public static ArticleBackup create(LocalDate backupDate, String s3Bucket, String s3Key, Long articleCount) {
        return new ArticleBackup(backupDate, s3Bucket, s3Key, articleCount);
    }

    public void updateArticleCount(Long articleCount) {
        this.articleCount = articleCount;
    }
}
