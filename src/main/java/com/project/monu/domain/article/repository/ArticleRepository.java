package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
//기본 CRUD 담당
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {

    // url 중복 체크 메서드
    boolean existsBySourceUrl(String sourceUrl);

    // 날짜 단위 백업을 위해 발행 시각이 [start, end) 범위에 들어오는 기사만 조회합니다.
    // end를 포함하지 않으면 자정 경계의 기사가 다음 날짜 백업과 중복되지 않습니다.
    List<Article> findByPublishDateGreaterThanEqualAndPublishDateLessThan(Instant start, Instant end);

    Optional<Article> findByIdAndDeletedAtIsNull(UUID id);
}
