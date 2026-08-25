package com.project.monu.domain.article.service;

import com.project.monu.domain.article.backup.ArticleBackupRecord;
import com.project.monu.domain.article.backup.ArticleBackupStorage;
import com.project.monu.domain.article.dto.response.ArticleBackupResultDto;
import com.project.monu.domain.article.dto.response.ArticleRestoreResultDto;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleBackup;
import com.project.monu.domain.article.entity.ArticleRestore;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.repository.ArticleBackupRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleRestoreRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleBackupService {

    // 지금은 로컬 파일 저장소를 쓰지만, 기존 엔티티 컬럼명이 s3Bucket이라 저장소 이름을 이 값에 기록합니다.
    private static final String STORAGE_NAME = "local";

    // 저장소 key는 로컬 파일 경로이면서, 나중에 S3 object key로도 그대로 사용할 수 있게 잡았습니다.
    private static final String BACKUP_PREFIX = "article-backups/";

    // 요구사항의 "날짜 단위"는 서비스 운영 시간대인 한국 시간 기준 하루로 해석합니다.
    private static final ZoneId BACKUP_ZONE = ZoneId.of("Asia/Seoul");

    private final ArticleRepository articleRepository;
    private final ArticleSourceRepository articleSourceRepository;
    private final ArticleBackupRepository articleBackupRepository;
    private final ArticleRestoreRepository articleRestoreRepository;
    private final ArticleBackupStorage backupStorage;

    // ArticleBackupRecord 객체를 JSON 문자열로 쓰고, JSON 문자열을 다시 객체로 읽기 위한 변환기입니다.
    private final ObjectMapper objectMapper;

    /**
     * 지정한 날짜에 발행된 기사들을 JSONL 파일로 백업합니다.
     *
     * JSONL은 한 줄에 JSON 객체 1개를 저장하는 형식입니다.
     * 기사 수가 많아져도 줄 단위로 읽고 비교하기 쉬워서 백업 파일에 적합합니다.
     */
    @Transactional
    public ArticleBackupResultDto backup(LocalDate date) {
        // LocalDate는 시간대 정보가 없으므로, 한국 시간 기준 하루를 Instant 범위로 변환해 DB를 조회합니다.
        // 예: 2026-08-21 KST => 2026-08-20T15:00:00Z 이상, 2026-08-21T15:00:00Z 미만
        var start = date.atStartOfDay(BACKUP_ZONE).toInstant();
        var end = date.plusDays(1).atStartOfDay(BACKUP_ZONE).toInstant();

        List<Article> articles =
                articleRepository.findByPublishDateGreaterThanEqualAndPublishDateLessThan(start, end);

        // 백업 파일은 JSONL 형식입니다. 기사 1건을 JSON 1줄로 저장해 복구 시 줄 단위로 읽습니다.
        String content = articles.stream()
                .map(ArticleBackupRecord::from)
                .map(this::toJson)
                .collect(Collectors.joining(System.lineSeparator()));

        String key = backupKey(date);
        backupStorage.save(key, content);

        // s3_key unique 제약 때문에 같은 날짜 백업을 다시 실행하면 기존 이력을 갱신합니다.
        // 파일은 같은 key에 덮어쓰고, DB 이력의 articleCount도 최신 백업 결과에 맞춥니다.
        ArticleBackup backup = articleBackupRepository.findByS3Key(key)
                .map(existingBackup -> {
                    existingBackup.updateArticleCount((long) articles.size());
                    return existingBackup;
                })
                .orElseGet(() -> ArticleBackup.create(
                        date,
                        STORAGE_NAME,
                        key,
                        (long) articles.size()
                ));
        articleBackupRepository.save(backup);

        return new ArticleBackupResultDto(date, STORAGE_NAME, key, articles.size());
    }

    /**
     * 백업 파일과 현재 DB를 비교해서 유실된 기사만 다시 저장합니다.
     *
     * 중복 판단 기준은 sourceUrl입니다.
     * Article 테이블에도 source_url unique 제약이 있으므로, 같은 원문 링크는 같은 기사로 봅니다.
     */
    @Transactional
    public ArticleRestoreResultDto restore(LocalDate date) {
        String key = backupKey(date);

        // 백업 파일이 없으면 복구 기준 데이터가 없다는 뜻이므로, 조용히 넘어가지 않고 실패로 알립니다.
        if (!backupStorage.exists(key)) {
            throw new IllegalArgumentException("백업 파일이 없습니다: " + key);
        }

        // 복구 이력은 어떤 백업 이력을 기준으로 복구했는지 남겨야 하므로, 해당 날짜의 최신 백업 이력을 찾습니다.
        ArticleBackup backup = articleBackupRepository.findTopByBackupDateOrderByCreatedAtDesc(date)
                .orElseThrow(() -> new IllegalArgumentException("백업 이력이 없습니다: " + date));

        // JSONL은 줄 단위 파일이라 빈 줄은 무시하고, 나머지 줄을 ArticleBackupRecord로 복원합니다.
        List<ArticleBackupRecord> records = Arrays.stream(backupStorage.load(key).split("\\R"))
                .filter(line -> !line.isBlank())
                .map(this::fromJson)
                .toList();

        long restoredCount = 0;

        for (ArticleBackupRecord record : records) {
            // 현재 DB에 이미 같은 원문 링크가 있으면 정상 데이터로 보고 복구 대상에서 제외합니다.
            if (articleRepository.existsBySourceUrl(record.sourceUrl())) {
                continue;
            }

            // 백업 파일에는 sourceName만 저장하므로, 실제 Article 엔티티에는 DB의 ArticleSource를 다시 연결합니다.
            ArticleSource source = articleSourceRepository.findByName(record.sourceName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "기사 출처가 없습니다: " + record.sourceName()
                    ));

            Article restoredArticle = Article.builder()
                    .source(source)
                    .sourceUrl(record.sourceUrl())
                    .title(record.title())
                    .publishDate(record.publishDate())
                    .summary(record.summary())
                    .build();

            articleRepository.save(restoredArticle);
            restoredCount++;
        }

        // 실제로 복구된 기사가 0건이어도 복구 작업을 수행했다는 사실은 이력으로 남깁니다.
        ArticleRestore restore = ArticleRestore.create(date, restoredCount, backup);
        articleRestoreRepository.save(restore);

        return new ArticleRestoreResultDto(date, key, restoredCount);
    }

    private String backupKey(LocalDate date) {
        // 날짜별 백업 파일 위치를 한 곳에서 만들면, S3 전환 시에도 key 규칙을 유지하기 쉽습니다.
        return BACKUP_PREFIX + date + ".jsonl";
    }

    private String toJson(ArticleBackupRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JacksonException e) {
            throw new IllegalStateException("기사 백업 JSON 변환에 실패했습니다.", e);
        }
    }

    private ArticleBackupRecord fromJson(String line) {
        try {
            return objectMapper.readValue(line, ArticleBackupRecord.class);
        } catch (JacksonException e) {
            throw new IllegalStateException("기사 백업 JSON 읽기에 실패했습니다.", e);
        }
    }
}
