package com.project.monu.domain.comment.service;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.CommentLikeDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentSearchCondition;
import com.project.monu.domain.comment.dto.request.CommentSortType;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.entity.Comment;
import com.project.monu.domain.comment.entity.CommentLike;
import com.project.monu.domain.comment.exception.InvalidCommentSortDirectionException;
import com.project.monu.domain.comment.repository.CommentLikeRepository;
import com.project.monu.domain.comment.repository.CommentQueryResult;
import com.project.monu.domain.comment.repository.CommentRepository;
import com.project.monu.domain.notification.event.CommentLikedEvent;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.dto.CursorPageResponse;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public CommentDto create(CommentCreateRequest request) {

        Article article = articleRepository.findByIdAndDeletedAtIsNull(request.articleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Comment comment = new Comment(article, user, request.content());

        Comment savedComment = commentRepository.save(comment);
        article.increaseCommentCount();

        return new CommentDto(
                savedComment.getId(),
                article.getId(),
                user.getId(),
                user.getNickname(),
                savedComment.getContent(),
                0L,
                false,
                savedComment.getCreatedAt());
    }

    @Transactional
    @Override
    public CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
        Comment comment = getActiveComment(commentId);

        if (!comment.getUser().getId().equals(requestUserId)) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.updateContent(request.content());

        long likeCount = commentLikeRepository.countByComment_Id(commentId);
        boolean likedByMe = commentLikeRepository
                .existsByComment_IdAndLikedBy_Id(commentId, requestUserId);

        return new CommentDto(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                likeCount,
                likedByMe,
                comment.getCreatedAt()
        );
    }

    @Transactional
    @Override
    public void delete(UUID commentId) {
        Comment comment = getActiveComment(commentId);

        comment.delete();
        comment.getArticle().decreaseCommentCount();
    }

    @Transactional
    @Override
    public void hardDelete(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (comment.getDeletedAt() == null) {
            comment.getArticle().decreaseCommentCount();
        }

        commentLikeRepository.deleteAllByComment_Id(commentId);
        commentRepository.delete(comment);
    }

    @Transactional
    @Override
    public void hardDeleteAllByUserId(UUID userId) {
        List<Comment> comments = commentRepository.findAllByUser_Id(userId);

        comments.stream()
                .filter(comment -> comment.getDeletedAt() == null)
                .forEach(comment -> comment.getArticle().decreaseCommentCount());

        commentLikeRepository.deleteAllByComment_User_Id(userId);
        commentLikeRepository.deleteAllByLikedBy_Id(userId);
        commentRepository.deleteAll(comments);
    }

    @Transactional
    @Override
    public CommentLikeDto like(UUID commentId, UUID requestUserId) {
        Comment comment = getActiveComment(commentId);

        User user = userRepository.findById(requestUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 중복 검사
        if (commentLikeRepository.existsByComment_IdAndLikedBy_Id(commentId, requestUserId)) {
            throw new BusinessException(ErrorCode.COMMENT_LIKE_ALREADY_EXISTS);
        }

        CommentLike commentLike = new CommentLike(comment, user);
        CommentLike savedLike = commentLikeRepository.save(commentLike);

        long likeCount = commentLikeRepository.countByComment_Id(commentId);

        eventPublisher.publishEvent(new CommentLikedEvent(
                comment.getUser().getId(),
                requestUserId,
                user.getNickname(),
                commentId
        ));

        return new CommentLikeDto(
                savedLike.getId(),
                requestUserId,
                savedLike.getCreatedAt(),
                comment.getId(),
                comment.getArticle().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                likeCount,
                comment.getCreatedAt()
        );
    }

    @Transactional
    @Override
    public void unlike(UUID commentId, UUID requestUserId) {
        getActiveComment(commentId);

        CommentLike commentLike = commentLikeRepository
                .findByComment_IdAndLikedBy_Id(commentId, requestUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_LIKE_NOT_FOUND));

        commentLikeRepository.delete(commentLike);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<CommentDto> getComments(
            UUID articleId,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit,
            UUID requestUserId
    ) {
        if (limit < 1) {
            throw new BusinessException(ErrorCode.COMMENT_INVALID_LIMIT);
        }

        CommentSortType sortType = CommentSortType.from(orderBy);

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            throw new InvalidCommentSortDirectionException();
        }

        CommentSearchCondition condition = new CommentSearchCondition(
                articleId,
                sortType,
                sortDirection,
                cursor,
                after,
                limit,
                requestUserId
        );

        List<CommentQueryResult> results = commentRepository.searchByCursor(condition);

        boolean hasNext = results.size() > limit;

        List<CommentQueryResult> pageResults = results.stream()
                .limit(limit)
                .toList();

        List<CommentDto> content = pageResults.stream()
                .map(result -> {
                    Comment comment = result.comment();

                    return new CommentDto(
                            comment.getId(),
                            comment.getArticle().getId(),
                            comment.getUser().getId(),
                            comment.getUser().getNickname(),
                            comment.getContent(),
                            result.likeCount(),
                            result.likedByMe(),
                            comment.getCreatedAt()
                    );
                })
                .toList();

        long totalElements = commentRepository.countByCondition(condition);

        String nextCursor = null;
        Instant nextAfter = null;

        if (hasNext && !pageResults.isEmpty()) {
            CommentQueryResult lastResult = pageResults.get(pageResults.size() - 1);

            nextCursor = createNextCursor(lastResult, sortType);
            nextAfter = lastResult.comment().getCreatedAt();
        }

        return CursorPageResponse.of(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                totalElements,
                hasNext
        );
    }

    // 댓글 단건 조회
    private Comment getActiveComment(UUID commentId) {
        return commentRepository.findById(commentId)
                .filter(comment -> comment.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private String createNextCursor(
            CommentQueryResult result,
            CommentSortType sortType
    ) {
        String value = switch (sortType) {
            case CREATED_AT -> result.comment().getCreatedAt().toString();
            case LIKE_COUNT -> String.valueOf(result.likeCount());
        };

        return value + "_" + result.comment().getId();
    }


}
