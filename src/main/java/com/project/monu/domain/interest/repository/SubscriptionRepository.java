package com.project.monu.domain.interest.repository;

import com.project.monu.domain.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    boolean existsByUserIdAndInterest_Id(UUID userId, UUID interestId);

    Optional<Subscription> findByUserIdAndInterest_Id(UUID userId, UUID interestId);

    void deleteAllByInterest_Id(UUID interestId);

    @Query("select s.interest.id from Subscription s where s.userId = :userId and s.interest.id in :interestIds")
    List<UUID> findSubscribedInterestIds(@Param("userId") UUID userId, @Param("interestIds") List<UUID> interestIds);

    @Query("""
        select s.userId
        from Subscription s
        where s.interest.id = :interestId
        """)
    List<UUID> findUserIdsByInterestId(@Param("interestId") UUID interestId);

    // User 물리 삭제를 위한 구독자수 줄이기
    List<Subscription> findAllByUserId(UUID userId);
}
