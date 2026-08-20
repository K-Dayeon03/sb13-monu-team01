package com.project.monu.domain.interest.service;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.repository.InterestRepository;
import com.project.monu.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 유사한 관심사가 존재합니다.");
    }
}