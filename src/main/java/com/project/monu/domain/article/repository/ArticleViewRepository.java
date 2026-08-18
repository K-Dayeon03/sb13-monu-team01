package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

    /**
     * 현재 사용자(userId)가 현재 페이지의 기사들(articleIds) 중 어떤 기사를 조회했는지 확인합니다.
     *
     * ArticleDto.viewedByMe 값을 만들기 위한 쿼리입니다.
     * 기사마다 ArticleView를 따로 조회하지 않고, 현재 페이지의 기사 ID들을 한 번에 조회합니다.
     */
    @Query("""
        select av.article.id
        from ArticleView av
        where av.viewerId = :userId
          and av.article.id in :articleIds
    """)
    Set<UUID> findViewedArticleIds(
            @Param("userId") UUID userId,
            @Param("articleIds") List<UUID> articleIds
    );
}
