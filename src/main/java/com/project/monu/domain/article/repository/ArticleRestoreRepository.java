package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.ArticleRestore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArticleRestoreRepository extends JpaRepository<ArticleRestore, UUID> {
}
