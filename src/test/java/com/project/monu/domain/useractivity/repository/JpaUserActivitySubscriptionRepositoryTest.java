package com.project.monu.domain.useractivity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        JpaAuditingConfig.class,
        QuerydslConfig.class,
        JpaUserActivitySubscriptionRepository.class
})
class JpaUserActivitySubscriptionRepositoryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserActivitySubscriptionRepository subscriptionRepository;

    @Test
    void 사용자가_구독한_관심사_목록을_조회한다() {
        UUID userId = UUID.randomUUID();

        Interest interest = Interest.create("경제");
        interest.addKeyword(Keyword.of("금리"));
        interest.addKeyword(Keyword.of("환율"));
        interest.increaseSubscriberCount();

        entityManager.persist(interest);

        Subscription subscription = Subscription.create(userId, interest);
        entityManager.persist(subscription);

        flushAndClear();

        List<SubscriptionDto> subscriptions = subscriptionRepository.findAllByUserId(userId);

        assertThat(subscriptions).hasSize(1);

        SubscriptionDto result = subscriptions.get(0);

        assertThat(result.id()).isEqualTo(subscription.getId());
        assertThat(result.interestId()).isEqualTo(interest.getId());
        assertThat(result.interestName()).isEqualTo("경제");
        assertThat(result.interestKeywords()).containsExactlyInAnyOrder("금리", "환율");
        assertThat(result.interestSubscriberCount()).isEqualTo(1L);
        assertThat(result.createdAt()).isNotNull();
    }

    @Test
    void 다른_사용자의_구독_관심사는_조회하지_않는다() {
        UUID requestUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        Interest interest = Interest.create("스포츠");
        interest.addKeyword(Keyword.of("야구"));

        entityManager.persist(interest);
        entityManager.persist(Subscription.create(otherUserId, interest));

        flushAndClear();

        List<SubscriptionDto> subscriptions = subscriptionRepository.findAllByUserId(requestUserId);

        assertThat(subscriptions).isEmpty();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}