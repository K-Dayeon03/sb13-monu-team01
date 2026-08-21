package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.ArticleBackup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ArticleBackupRepository extends JpaRepository<ArticleBackup, UUID> {

    // 같은 날짜 백업이 여러 번 실행될 수 있으므로, 복구 시에는 가장 최근 백업 이력을 사용합니다.
    Optional<ArticleBackup> findTopByBackupDateOrderByCreatedAtDesc(LocalDate backupDate);

    // s3_key에는 unique 제약이 있어 같은 파일 위치의 백업 이력을 중복 저장하지 않도록 확인합니다.
    Optional<ArticleBackup> findByS3Key(String s3Key);
}
