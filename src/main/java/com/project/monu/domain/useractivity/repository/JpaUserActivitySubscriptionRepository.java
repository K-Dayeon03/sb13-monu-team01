package com.project.monu.domain.useractivity.repository;

import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.entity.Subscription;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class JpaUserActivitySubscriptionRepository implements UserActivitySubscriptionRepository {

    private final EntityManager entityManager;

    public JpaUserActivitySubscriptionRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<SubscriptionDto> findAllByUserId(UUID userId) {
        List<Subscription> subscriptions = entityManager.createQuery("""
                        select distinct subscription
                        from Subscription subscription
                        join fetch subscription.interest interest
                        left join fetch interest.keywords
                        where subscription.userId = :userId
                        order by subscription.createdAt desc
                        """, Subscription.class)
                .setParameter("userId", userId)
                .getResultList();

        return subscriptions.stream()
                .map(this::toSubscriptionDto)
                .toList();
    }

    private SubscriptionDto toSubscriptionDto(Subscription subscription) {
        Interest interest = subscription.getInterest();

        return new SubscriptionDto(
                subscription.getId(),
                interest.getId(),
                interest.getName(),
                getKeywordValues(interest),
                interest.getSubscriberCount(),
                subscription.getCreatedAt()
        );
    }

    private List<String> getKeywordValues(Interest interest) {
        return interest.getKeywords().stream()
                .map(Keyword::getKeyword)
                .toList();
    }
}