package com.project.monu.domain.interest.repository;

import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.global.config.JpaAuditingConfig;
import com.project.monu.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingConfig.class})
class InterestRepositoryImplTest {

    @Autowired
    private InterestRepository interestRepository;

    @Test
    @DisplayName("이름에 키워드가 포함된 관심사만 조회된다")
    void searchByCursor_filtersByKeyword() {
        // given
        interestRepository.save(Interest.create("인공지능"));
        interestRepository.save(Interest.create("스포츠"));
        interestRepository.save(Interest.create("인공위성"));

        InterestSearchCondition condition = new InterestSearchCondition(
                "인공", InterestSortType.NAME, null, 10
        );

        // when
        List<Interest> result = interestRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Interest::getName)
                .containsExactlyInAnyOrder("인공지능", "인공위성");
    }

    @Test
    @DisplayName("구독자수 기준 내림차순으로 정렬된다")
    void searchByCursor_sortsBySubscriberCountDesc() {
        // given
        Interest low = Interest.create("낮은인기");
        Interest high = Interest.create("높은인기");
        for (int i = 0; i < 5; i++) high.increaseSubscriberCount();
        low.increaseSubscriberCount();

        interestRepository.save(low);
        interestRepository.save(high);

        InterestSearchCondition condition = new InterestSearchCondition(
                null, InterestSortType.SUBSCRIBER_COUNT, null, 10
        );

        // when
        List<Interest> result = interestRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Interest::getName)
                .containsExactly("높은인기", "낮은인기");
    }

    @Test
    @DisplayName("구독자수 커서 이후의 관심사를 조회한다")
    void searchByCursor_afterSubscriberCountCursor() {
        // given
        Interest a = Interest.create("A");
        Interest b = Interest.create("B");
        Interest c = Interest.create("C");
        for (int i = 0; i < 3; i++) a.increaseSubscriberCount();
        for (int i = 0; i < 2; i++) b.increaseSubscriberCount();
        c.increaseSubscriberCount();

        Interest savedA = interestRepository.save(a);
        interestRepository.save(b);
        interestRepository.save(c);

        String cursor = savedA.getSubscriberCount() + "_" + savedA.getId();
        InterestSearchCondition condition = new InterestSearchCondition(
                null, InterestSortType.SUBSCRIBER_COUNT, cursor, 10
        );

        // when
        List<Interest> result = interestRepository.searchByCursor(condition);

        // then
        assertThat(result).extracting(Interest::getName)
                .containsExactly("B", "C");
    }

}