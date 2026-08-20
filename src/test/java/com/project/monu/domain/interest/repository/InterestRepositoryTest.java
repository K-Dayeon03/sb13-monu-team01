package com.project.monu.domain.interest.repository;

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
class InterestRepositoryTest {

    @Autowired
    private InterestRepository interestRepository;

    @Test
    @DisplayName("저장된 모든 관심사의 이름 목록을 조회한다")
    void findAllNames_returnsAllInterestNames() {
        // given
        interestRepository.save(Interest.create("인공지능"));
        interestRepository.save(Interest.create("머신러닝"));

        // when
        List<String> names = interestRepository.findAllNames();

        // then
        assertThat(names).containsExactlyInAnyOrder("인공지능", "머신러닝");
    }
}