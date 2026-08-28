package com.project.monu.domain.useractivity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.monu.domain.useractivity.document.UserActivityDocument;
import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@DataMongoTest
@Testcontainers
class UserActivityMongoRepositoryTest {

    @Container
    static final MongoDBContainer mongoDBContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.mongodb.representation.uuid", () -> "standard");
    }

    @Autowired
    private UserActivityMongoRepository userActivityMongoRepository;

    @BeforeEach
    void setUp() {
        userActivityMongoRepository.deleteAll();
    }

    @Test
    void 사용자_활동_문서를_저장하고_조회한다() {
        UUID userId = UUID.randomUUID();
        UserActivityDocument document = UserActivityDocument.from(
                createResponse(userId, "user@email.com", "사용자")
        );

        userActivityMongoRepository.save(document);

        Optional<UserActivityDocument> result =
                userActivityMongoRepository.findById(userId);

        assertThat(result).isPresent();

        UserActivityResponse savedResponse = result.get().toResponse();

        assertThat(savedResponse.id()).isEqualTo(userId);
        assertThat(savedResponse.email()).isEqualTo("user@email.com");
        assertThat(savedResponse.nickname()).isEqualTo("사용자");
        assertThat(savedResponse.createdAt())
                .isEqualTo(Instant.parse("2026-08-28T00:00:00Z"));
        assertThat(savedResponse.subscriptions()).isEmpty();
        assertThat(savedResponse.comments()).isEmpty();
        assertThat(savedResponse.commentLikes()).isEmpty();
        assertThat(savedResponse.articleViews()).isEmpty();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void 같은_사용자_활동_문서를_다시_저장하면_최신_내용으로_덮어쓴다() {
        UUID userId = UUID.randomUUID();

        UserActivityDocument oldDocument = UserActivityDocument.from(
                createResponse(userId, "old@email.com", "이전사용자")
        );
        UserActivityDocument newDocument = UserActivityDocument.from(
                createResponse(userId, "new@email.com", "최신사용자")
        );

        userActivityMongoRepository.save(oldDocument);
        userActivityMongoRepository.save(newDocument);

        Optional<UserActivityDocument> result =
                userActivityMongoRepository.findById(userId);

        assertThat(result).isPresent();
        assertThat(userActivityMongoRepository.count()).isEqualTo(1);

        UserActivityResponse savedResponse = result.get().toResponse();

        assertThat(savedResponse.id()).isEqualTo(userId);
        assertThat(savedResponse.email()).isEqualTo("new@email.com");
        assertThat(savedResponse.nickname()).isEqualTo("최신사용자");
    }

    @Test
    void 사용자_활동_문서가_없으면_빈_결과를_반환한다() {
        UUID userId = UUID.randomUUID();

        Optional<UserActivityDocument> result =
                userActivityMongoRepository.findById(userId);

        assertThat(result).isEmpty();
    }

    private UserActivityResponse createResponse(UUID userId, String email, String nickname) {
        return new UserActivityResponse(
                userId,
                email,
                nickname,
                Instant.parse("2026-08-28T00:00:00Z"),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}