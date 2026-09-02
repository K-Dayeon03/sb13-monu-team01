package com.project.monu.domain.article.backup;

import com.project.monu.domain.article.entity.Article;

import java.time.Instant;
import java.util.List;

/**
 * 백업 파일에 기사 1건을 저장할 때 사용하는 데이터 형태입니다.
 *
 * Article 엔티티 전체를 그대로 파일에 넣지 않고, 복구에 꼭 필요한 값만 남깁니다.
 * 이렇게 하면 DB 내부 필드가 바뀌어도 백업 파일 구조를 비교적 안정적으로 유지할 수 있습니다.
 */
public record ArticleBackupRecord(
        // source_id는 환경마다 달라질 수 있으므로, 백업 파일에는 사람이 읽을 수 있는 sourceName을 남깁니다.
        String sourceName,
        // 복구 시 중복 판단 기준입니다. Article 테이블의 unique 제약도 source_url 기준입니다.
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,
        List<String> interestNames
) {

    public ArticleBackupRecord {
        interestNames = interestNames == null ? List.of() : List.copyOf(interestNames);
    }

    public static ArticleBackupRecord from(Article article) {
        // Article 엔티티에서 복구에 필요한 값만 뽑아 백업 전용 record로 변환합니다.
        return new ArticleBackupRecord(
                article.getSource().getName(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                List.of()
        );
    }
}
