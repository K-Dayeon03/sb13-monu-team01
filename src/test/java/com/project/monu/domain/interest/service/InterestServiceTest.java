package com.project.monu.domain.interest.service;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.repository.InterestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.global.dto.CursorPageResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.monu.domain.interest.exception.InterestDuplicateException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    private InterestService interestService;

    @BeforeEach
    void setUp() {
        interestService = new InterestService(interestRepository);
    }

    @Test
    @DisplayName("새로운 이름으로 관심사를 등록하면 InterestDto를 반환한다")
    void register_success() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest("인공지능", List.of("AI", "머신러닝"));
        when(interestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        InterestDto result = interestService.register(request);

        // then
        assertThat(result.name()).isEqualTo("인공지능");
        assertThat(result.keywords()).containsExactlyInAnyOrder("AI", "머신러닝");
        assertThat(result.subscribedByMe()).isFalse();
        verify(interestRepository).save(any());
    }
    @Test
    @DisplayName("기존 관심사와 80% 이상 유사한 이름이면 예외가 발생한다")
    void register_throwsException_whenSimilarNameExists() {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest("인공지능개발", List.of("AI"));
        when(interestRepository.findAllNames()).thenReturn(List.of("인공지능개론"));

        // when & then
        assertThatThrownBy(() -> interestService.register(request))
                .isInstanceOf(InterestDuplicateException.class);
    }

    @Test
    @DisplayName("조건에 맞는 관심사 목록을 커서 페이지 응답으로 반환한다")
    void getInterests_returnsPagedResponse() {
        // given
        InterestSearchCondition condition = new InterestSearchCondition(null, InterestSortType.SUBSCRIBER_COUNT, null, 10);

        Interest interest = Interest.create("인공지능");
        interest.addKeyword(Keyword.of("AI"));

        when(interestRepository.searchByCursor(any())).thenReturn(List.of(interest));
        when(interestRepository.countByCondition(any())).thenReturn(1L);

        // when
        CursorPageResponse<InterestDto> response = interestService.getInterests(condition);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).name()).isEqualTo("인공지능");
        assertThat(response.hasNext()).isFalse();
        assertThat(response.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("조회 결과가 size보다 많으면 hasNext가 true이고 초과분은 잘린다")
    void getInterests_setsHasNextTrue_whenMoreThanSizeResults() {
        // given
        InterestSearchCondition condition = new InterestSearchCondition(null, InterestSortType.SUBSCRIBER_COUNT, null, 1);

        Interest first = Interest.create("인공지능");
        Interest second = Interest.create("스포츠");
        ReflectionTestUtils.setField(first, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(second, "id", UUID.randomUUID());

        when(interestRepository.searchByCursor(any())).thenReturn(List.of(first, second));
        when(interestRepository.countByCondition(any())).thenReturn(2L);

        // when
        CursorPageResponse<InterestDto> response = interestService.getInterests(condition);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
    }

}