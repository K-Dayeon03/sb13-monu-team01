package com.project.monu.domain.interest.service;


import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.domain.interest.repository.InterestRepository;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.global.dto.CursorPageResponse;
import com.project.monu.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterestServiceTest {

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ArticleInterestRepository articleInterestRepository;

    private InterestService interestService;

    @BeforeEach
    void setUp() {
        interestService = new InterestService(interestRepository, subscriptionRepository, articleInterestRepository);
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

    @Test
    @DisplayName("관심사의 키워드를 수정하면 변경된 InterestDto를 반환한다")
    void update_success() {
        // given
        Interest interest = Interest.create("인공지능");
        interest.addKeyword(Keyword.of("AI"));
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

        when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));

        com.project.monu.domain.interest.dto.request.InterestUpdateRequest request =
                new com.project.monu.domain.interest.dto.request.InterestUpdateRequest(List.of("머신러닝", "딥러닝"));

        // when
        InterestDto result = interestService.update(interest.getId(), request);

        // then
        assertThat(result.keywords()).containsExactlyInAnyOrder("머신러닝", "딥러닝");
    }

    @Test
    @DisplayName("존재하지 않는 관심사를 수정하면 예외가 발생한다")
    void update_throwsException_whenInterestNotFound() {
        // given
        UUID interestId = UUID.randomUUID();
        when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

        com.project.monu.domain.interest.dto.request.InterestUpdateRequest request =
                new com.project.monu.domain.interest.dto.request.InterestUpdateRequest(List.of("AI"));

        // when & then
        assertThatThrownBy(() -> interestService.update(interestId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("관심사를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하는 관심사를 삭제하면 관심사와 구독 내역이 함께 삭제된다")
    void delete_success() {
        // given
        Interest interest = Interest.create("인공지능");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
        when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));

        // when
        interestService.delete(interest.getId());

        // then
        verify(subscriptionRepository).deleteAllByInterest_Id(interest.getId());
        verify(articleInterestRepository).deleteAllByInterest_Id(interest.getId());
        verify(interestRepository).delete(interest);
    }

    @Test
    @DisplayName("존재하지 않는 관심사를 삭제하면 예외가 발생한다")
    void delete_throwsException_whenInterestNotFound() {
        // given
        UUID interestId = UUID.randomUUID();
        when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> interestService.delete(interestId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("관심사를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하는 관심사를 구독하면 SubscriptionDto를 반환한다")
    void subscribe_success() {
        // given
        UUID userId = UUID.randomUUID();
        Interest interest = Interest.create("인공지능");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

        when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));
        when(subscriptionRepository.existsByUserIdAndInterest_Id(userId, interest.getId())).thenReturn(false);
        when(subscriptionRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(interestRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SubscriptionDto result = interestService.subscribe(userId, interest.getId());

        // then
        assertThat(result.interestId()).isEqualTo(interest.getId());
        assertThat(result.interestName()).isEqualTo("인공지능");
        assertThat(interest.getSubscriberCount()).isEqualTo(1L);
        verify(subscriptionRepository).saveAndFlush(any());
        verify(interestRepository).saveAndFlush(interest);
    }

    @Test
    @DisplayName("동시 요청으로 구독 유니크 제약을 위반하면 이미 구독 중 예외로 변환된다")
    void subscribe_throwsSubscriptionAlreadyExists_whenUniqueConstraintViolated() {
        // given
        // existsBy 체크는 통과했지만(동시 요청 레이스 컨디션 상황을 재현), 실제 insert 시점에
        // DB 유니크 제약(uk_subscription_user_interest)에 걸려 DataIntegrityViolationException이 발생하는 케이스
        UUID userId = UUID.randomUUID();
        Interest interest = Interest.create("인공지능");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

        when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));
        when(subscriptionRepository.existsByUserIdAndInterest_Id(userId, interest.getId())).thenReturn(false);
        when(subscriptionRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("uk_subscription_user_interest violation"));

        // when & then
        assertThatThrownBy(() -> interestService.subscribe(userId, interest.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 구독 중인 관심사입니다.");
    }

    @Test
    @DisplayName("구독자 수 갱신 중 낙관적 락 충돌이 발생하면 동시 요청 예외로 변환된다")
    void subscribe_throwsInterestConcurrentUpdate_whenOptimisticLockConflict() {
        // given
        // 다른 트랜잭션이 먼저 subscriberCount를 갱신해 Interest의 @Version이 바뀐 상태에서
        // saveAndFlush를 호출하면 ObjectOptimisticLockingFailureException이 발생하는 케이스
        UUID userId = UUID.randomUUID();
        Interest interest = Interest.create("인공지능");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

        when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));
        when(subscriptionRepository.existsByUserIdAndInterest_Id(userId, interest.getId())).thenReturn(false);
        when(subscriptionRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(interestRepository.saveAndFlush(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Interest.class, interest.getId()));

        // when & then
        assertThatThrownBy(() -> interestService.subscribe(userId, interest.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("다른 요청과 동시에 처리되어 반영에 실패했습니다. 다시 시도해주세요.");
    }

    @Test
    @DisplayName("존재하지 않는 관심사를 구독하면 예외가 발생한다")
    void subscribe_throwsException_whenInterestNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        when(interestRepository.findById(interestId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> interestService.subscribe(userId, interestId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("관심사를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이미 구독 중인 관심사를 다시 구독하면 예외가 발생한다")
    void subscribe_throwsException_whenAlreadySubscribed() {
        // given
        UUID userId = UUID.randomUUID();
        Interest interest = Interest.create("인공지능");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

        when(interestRepository.findById(interest.getId())).thenReturn(Optional.of(interest));
        when(subscriptionRepository.existsByUserIdAndInterest_Id(userId, interest.getId())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> interestService.subscribe(userId, interest.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 구독 중인 관심사입니다.");
    }

    @Test
    @DisplayName("조건에 맞는 관심사 목록을 커서 페이지 응답으로 반환한다")
    void getInterests_returnsPagedResponse() {
        // given
        InterestSearchCondition condition = new InterestSearchCondition(
                null, InterestSortType.SUBSCRIBER_COUNT, null, null, null, 10
        );

        Interest interest = Interest.create("인공지능");
        interest.addKeyword(Keyword.of("AI"));
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());

        when(interestRepository.searchByCursor(any())).thenReturn(List.of(interest));
        when(interestRepository.countByCondition(any())).thenReturn(1L);

        // when
        CursorPageResponse<InterestDto> response = interestService.getInterests(condition, null);

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
        InterestSearchCondition condition = new InterestSearchCondition(
                null, InterestSortType.SUBSCRIBER_COUNT, null, null, null, 1
        );

        Interest first = Interest.create("인공지능");
        Interest second = Interest.create("스포츠");
        ReflectionTestUtils.setField(first, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(second, "id", UUID.randomUUID());

        when(interestRepository.searchByCursor(any())).thenReturn(List.of(first, second));
        when(interestRepository.countByCondition(any())).thenReturn(2L);

        // when
        CursorPageResponse<InterestDto> response = interestService.getInterests(condition, null);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.nextCursor()).isNotNull();
    }

    @Test
    @DisplayName("구독 중인 관심사를 구독취소하면 구독이 삭제되고 구독자 수가 감소한다")
    void unsubscribe_success() {
        // given
        UUID userId = UUID.randomUUID();
        Interest interest = Interest.create("인공지능");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
        interest.increaseSubscriberCount();
        Subscription subscription = Subscription.create(userId, interest);

        when(subscriptionRepository.findByUserIdAndInterest_Id(userId, interest.getId()))
                .thenReturn(Optional.of(subscription));
        when(interestRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        interestService.unsubscribe(userId, interest.getId());

        // then
        assertThat(interest.getSubscriberCount()).isEqualTo(0L);
        verify(subscriptionRepository).delete(subscription);
        verify(interestRepository).saveAndFlush(interest);
    }

    @Test
    @DisplayName("구독취소로 구독자 수 갱신 중 낙관적 락 충돌이 발생하면 동시 요청 예외로 변환된다")
    void unsubscribe_throwsInterestConcurrentUpdate_whenOptimisticLockConflict() {
        // given
        UUID userId = UUID.randomUUID();
        Interest interest = Interest.create("인공지능");
        ReflectionTestUtils.setField(interest, "id", UUID.randomUUID());
        interest.increaseSubscriberCount();
        Subscription subscription = Subscription.create(userId, interest);

        when(subscriptionRepository.findByUserIdAndInterest_Id(userId, interest.getId()))
                .thenReturn(Optional.of(subscription));
        when(interestRepository.saveAndFlush(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Interest.class, interest.getId()));

        // when & then
        assertThatThrownBy(() -> interestService.unsubscribe(userId, interest.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("다른 요청과 동시에 처리되어 반영에 실패했습니다. 다시 시도해주세요.");
    }

    @Test
    @DisplayName("구독하지 않은 관심사를 구독취소하면 예외가 발생한다")
    void unsubscribe_throwsException_whenSubscriptionNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        when(subscriptionRepository.findByUserIdAndInterest_Id(userId, interestId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> interestService.unsubscribe(userId, interestId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("구독 내역을 찾을 수 없습니다.");
    }

}
