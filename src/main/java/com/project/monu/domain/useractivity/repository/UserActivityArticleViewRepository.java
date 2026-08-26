package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import java.util.List;
import java.util.UUID;

public interface UserActivityArticleViewRepository {

    List<ArticleViewDto> findAllByUserId(UUID userId);
}