package com.project.monu.domain.interest.repository;

import com.project.monu.domain.interest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

    @Query("select i.name from Interest i")
    List<String> findAllNames();

    List<Interest> findByNameIn(List<String> names);
}
