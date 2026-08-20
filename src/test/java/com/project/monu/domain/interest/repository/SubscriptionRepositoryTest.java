package com.project.monu.domain.interest.repository;

import com.project.monu.global.config.QuerydslConfig;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
class SubscriptionRepositoryTest {

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Test
    @DisplayName("사용자가 이미 구독한 관심사인지 확인한다")
    void existsByUserIdAndInterest_Id_returnsTrue_whenAlreadySubscribed() {
        // given
        Interest interest = interestRepository.save(Interest.create("인공지능"));
        UUID userId = UUID.randomUUID();
        subscriptionRepository.save(Subscription.create(userId, interest));

        // when
        boolean exists = subscriptionRepository.existsByUserIdAndInterest_Id(userId, interest.getId());

        // then
        assertThat(exists).isTrue();
    }
}