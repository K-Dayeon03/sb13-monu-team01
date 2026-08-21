package com.project.monu.domain.interest.repository;

import com.project.monu.domain.interest.entity.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface KeywordRepository extends JpaRepository<Keyword, UUID> {

    @Query("SELECT k FROM Keyword k JOIN fetch k.interest")
    List<Keyword> findAllWithInterest();
}
