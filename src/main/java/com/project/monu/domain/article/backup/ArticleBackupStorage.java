package com.project.monu.domain.article.backup;

/**
 * 백업 파일 저장소를 추상화합니다.
 *
 * 지금은 로컬 파일로 저장하지만, 나중에 AWS S3를 붙일 때도
 * 서비스 코드는 그대로 두고 이 인터페이스의 구현체만 교체하면 됩니다.
 */
public interface ArticleBackupStorage {

    // key는 S3 object key처럼 "article-backups/2026-08-21.jsonl" 형식으로 사용합니다.
    // 로컬 구현체는 이 key를 파일 경로로 해석하고, S3 구현체는 object key로 해석하면 됩니다.
    void save(String key, String content);

    // 복구할 때 저장된 백업 파일 내용을 문자열로 읽어옵니다.
    String load(String key);

    // 복구 요청 날짜에 해당하는 백업 파일이 있는지 먼저 확인합니다.
    boolean exists(String key);
}
