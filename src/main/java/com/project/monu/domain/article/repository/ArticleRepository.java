package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
//기본 CRUD 담당
public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {
}
