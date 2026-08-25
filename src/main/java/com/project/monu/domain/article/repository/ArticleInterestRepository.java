package com.project.monu.domain.article.repository;

import com.project.monu.domain.article.entity.ArticleInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

// 관심사 매칭 기록 저장용
public interface ArticleInterestRepository extends JpaRepository<ArticleInterest, UUID> {

    void deleteAllByArticle_Id(UUID articleId);
}
