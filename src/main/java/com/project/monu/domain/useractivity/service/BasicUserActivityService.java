package com.project.monu.domain.useractivity.service;

import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.useractivity.document.UserActivityDocument;
import com.project.monu.domain.useractivity.dto.UserActivityCommentLikeResponse;
import com.project.monu.domain.useractivity.dto.UserActivityResponse;
import com.project.monu.domain.useractivity.repository.UserActivityArticleViewRepository;
import com.project.monu.domain.useractivity.repository.UserActivityCommentLikeRepository;
import com.project.monu.domain.useractivity.repository.UserActivityCommentRepository;
import com.project.monu.domain.useractivity.repository.UserActivityMongoRepository;
import com.project.monu.domain.useractivity.repository.UserActivitySubscriptionRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BasicUserActivityService implements UserActivityService {

    private final UserRepository userRepository;
    private final UserActivityMongoRepository mongoRepository;
    private final UserActivitySubscriptionRepository subscriptionRepository;
    private final UserActivityCommentRepository commentRepository;
    private final UserActivityCommentLikeRepository commentLikeRepository;
    private final UserActivityArticleViewRepository articleViewRepository;

    public BasicUserActivityService(
            UserRepository userRepository,
            UserActivityMongoRepository mongoRepository,
            UserActivitySubscriptionRepository subscriptionRepository,
            UserActivityCommentRepository commentRepository,
            UserActivityCommentLikeRepository commentLikeRepository,
            UserActivityArticleViewRepository articleViewRepository
    ) {
        this.userRepository = userRepository;
        this.mongoRepository = mongoRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.articleViewRepository = articleViewRepository;
    }

    @Override
    @Transactional
    public UserActivityResponse getUserActivity(UUID userId) {
        UserActivityResponse latestActivity = loadLatestUserActivity(userId);
        saveUserActivityReadModel(latestActivity);

        return latestActivity;
    }

    private void saveUserActivityReadModel(UserActivityResponse response) {
        mongoRepository.save(UserActivityDocument.from(response, Instant.now()));
    }

    private UserActivityResponse loadLatestUserActivity(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<SubscriptionDto> subscriptions = subscriptionRepository.findAllByUserId(userId);
        List<CommentDto> comments = commentRepository.findAllByUserId(userId);
        List<UserActivityCommentLikeResponse> commentLikes =
                commentLikeRepository.findAllByUserId(userId);
        List<ArticleViewDto> articleViews = articleViewRepository.findAllByUserId(userId);

        return new UserActivityResponse(
                userId,
                user.getEmail(),
                user.getNickname(),
                user.getCreatedAt(),
                subscriptions,
                comments,
                commentLikes,
                articleViews
        );
    }
}
