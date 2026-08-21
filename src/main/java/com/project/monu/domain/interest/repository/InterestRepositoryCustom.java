package com.project.monu.domain.interest.repository;

import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.entity.Interest;

import java.util.List;

public interface InterestRepositoryCustom {

    List<Interest> searchByCursor(InterestSearchCondition condition);

    long countByCondition(InterestSearchCondition condition);
}