package com.project.monu.domain.interest;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.domain.interest.repository.InterestRepository;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.global.constant.RequestHeaders;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 관심사 도메인 통합 테스트.
 *
 * 서비스 계층 단위 테스트(InterestServiceTest)는 리포지토리를 Mock으로 대체하기 때문에
 * DB 유니크 제약(uk_subscription_user_interest)이나 Interest의 @Version 낙관적 락처럼
 * 실제 DB(H2)와 맞닿아야만 검증되는 동작은 커버하지 못한다.
 * 이 테스트는 Controller -> Service -> Repository -> 실제 DB까지 전 구간을 통해
 * 그 부분을 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Transactional
class InterestIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("관심사를 등록하면 실제 DB에 반영된다")
    void register_success_persistsToDatabase() throws Exception {
        // given
        InterestRegisterRequest request = new InterestRegisterRequest("인공지능", List.of("AI"));

        // when & then
        mockMvc.perform(post("/api/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("인공지능"));

        assertThat(interestRepository.findAllNames()).contains("인공지능");
    }

    @Test
    @DisplayName("짧은 이름이 기존 이름과 절대 편집 거리 1 이내이면 등록 시 409를 반환한다")
    void register_throws409_whenShortNameIsSimilar() throws Exception {
        // given
        // 길이 4 이하 이름은 비율(80%) 대신 절대 편집 거리로 판단하도록 바꾼
        // InterestSimilarityCalculator 변경이 실제 등록 API에서도 그대로 동작하는지 확인한다.
        interestRepository.saveAndFlush(Interest.create("AI"));
        InterestRegisterRequest request = new InterestRegisterRequest("BI", List.of("금융"));

        // when & then
        mockMvc.perform(post("/api/interests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INTEREST_DUPLICATION"));
    }

    @Test
    @DisplayName("구독하면 실제 DB에 반영되고 구독자 수가 증가한다")
    void subscribe_success_persistsAndIncreasesCount() throws Exception {
        // given
        Interest interest = interestRepository.saveAndFlush(Interest.create("인공지능"));
        UUID userId = UUID.randomUUID();

        // when
        mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interest.getId())
                        .header(RequestHeaders.REQUEST_USER_ID, userId.toString()))
                .andExpect(status().isCreated());

        // then
        entityManager.clear();
        Interest reloaded = interestRepository.findById(interest.getId()).orElseThrow();
        assertThat(reloaded.getSubscriberCount()).isEqualTo(1L);
        assertThat(subscriptionRepository.existsByUserIdAndInterest_Id(userId, interest.getId())).isTrue();
    }

    @Test
    @DisplayName("이미 구독 중인 관심사를 다시 구독하면 409를 반환한다")
    void subscribe_throws409_whenAlreadySubscribed() throws Exception {
        // given
        Interest interest = interestRepository.saveAndFlush(Interest.create("인공지능"));
        UUID userId = UUID.randomUUID();
        subscriptionRepository.saveAndFlush(Subscription.create(userId, interest));

        // when & then
        mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interest.getId())
                        .header(RequestHeaders.REQUEST_USER_ID, userId.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SUBSCRIPTION_DUPLICATION"));
    }

    @Test
    @DisplayName("Subscription 테이블은 사용자-관심사 조합에 실제 DB 유니크 제약을 갖는다")
    void subscriptionRepository_enforcesUniqueConstraint_atDatabaseLevel() {
        // given
        // 서비스 계층의 existsBy 선확인을 우회해서 DB 제약 자체가 살아있는지 직접 검증한다.
        // 동시 요청이 existsBy를 둘 다 통과했을 때 최종 방어선 역할을 하는 게 바로 이 제약이다.
        Interest interest = interestRepository.saveAndFlush(Interest.create("인공지능"));
        UUID userId = UUID.randomUUID();
        subscriptionRepository.saveAndFlush(Subscription.create(userId, interest));

        // when & then
        assertThatThrownBy(() ->
                subscriptionRepository.saveAndFlush(Subscription.create(userId, interest)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Interest는 다른 트랜잭션이 먼저 버전을 갱신한 상태에서 저장을 시도하면 낙관적 락 예외가 발생한다")
    void interestRepository_throwsOptimisticLockException_whenVersionConflicts() {
        // given
        Interest interest = interestRepository.saveAndFlush(Interest.create("인공지능"));
        entityManager.clear();

        // 다른 트랜잭션이 먼저 커밋해서 버전이 올라간 상황을 네이티브 쿼리로 흉내낸다.
        // (ORM을 거치지 않고 DB 값만 직접 바꿔서, interest는 여전히 옛 버전을 들고 있는
        //  detach된 상태가 된다.)
        entityManager.createNativeQuery("UPDATE interest SET version = version + 1 WHERE id = ?1")
                .setParameter(1, interest.getId())
                .executeUpdate();
        entityManager.clear();

        // when & then
        interest.increaseSubscriberCount();
        assertThatThrownBy(() -> interestRepository.saveAndFlush(interest))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
