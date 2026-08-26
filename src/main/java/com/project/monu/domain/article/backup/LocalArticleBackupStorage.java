package com.project.monu.domain.article.backup;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 개발 환경에서 사용하는 로컬 파일 백업 저장소입니다.
 *
 * 실제 배포에서는 같은 ArticleBackupStorage 인터페이스를 구현하는
 * S3 저장소 클래스를 추가해서 교체하면 됩니다.
 */
@Component
@ConditionalOnProperty(
        name = "article.backup.storage",
        havingValue = "local",
        matchIfMissing = true
)
public class LocalArticleBackupStorage implements ArticleBackupStorage {

    // 실제 저장 위치는 프로젝트 실행 경로 기준 backups/ 아래입니다.
    // 예: backups/article-backups/2026-08-21.jsonl
    private static final Path ROOT_PATH = Path.of("backups");

    @Override
    public void save(String key, String content) {
        try {
            Path path = ROOT_PATH.resolve(key);
            // "article-backups/..."처럼 하위 폴더가 있는 key도 저장할 수 있도록 부모 폴더를 먼저 만듭니다.
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new IllegalStateException("백업 파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public String load(String key) {
        try {
            return Files.readString(ROOT_PATH.resolve(key));
        } catch (IOException e) {
            throw new IllegalStateException("백업 파일 읽기에 실패했습니다.", e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(ROOT_PATH.resolve(key));
    }

    @Override
    public String storageName() {
        return "local";
    }
}
