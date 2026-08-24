package com.project.monu.domain.interest.repository;

import com.project.monu.domain.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    boolean existsByUserIdAndInterest_Id(UUID userId, UUID interestId);

    Optional<Subscription> findByUserIdAndInterest_Id(UUID userId, UUID interestId);
}