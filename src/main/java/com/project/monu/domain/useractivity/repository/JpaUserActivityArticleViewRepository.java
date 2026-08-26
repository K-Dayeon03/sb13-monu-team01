package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.article.entity.ArticleView;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserActivityArticleViewRepository implements UserActivityArticleViewRepository {

    private final EntityManager entityManager;

    public JpaUserActivityArticleViewRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<ArticleViewDto> findAllByUserId(UUID userId) {
        return entityManager.createQuery("""
                        select articleView
                        from ArticleView articleView
                        join fetch articleView.viewer viewer
                        join fetch articleView.article article
                        join fetch article.source source
                        where viewer.id = :userId
                          and article.deletedAt is null
                        order by articleView.createdAt desc
                        """, ArticleView.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(ArticleViewDto::from)
                .toList();
    }
}