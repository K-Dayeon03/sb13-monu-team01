package com.project.monu.domain.comment.repository;

import com.project.monu.domain.comment.dto.request.CommentSearchCondition;

import java.util.List;

public interface CommentRepositoryCustom {

    List<CommentQueryResult> searchByCursor(CommentSearchCondition condition);

    long countByCondition(CommentSearchCondition condition);
}