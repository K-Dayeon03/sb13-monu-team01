package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.ArticleSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ArticleSourceRepository extends JpaRepository<ArticleSource, UUID> {
    
    // 출처 이름으로 조회
    Optional<ArticleSource> findByName(String name);
}
