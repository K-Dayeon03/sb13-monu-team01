package com.project.monu.domain.comment.service;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.CommentLikeDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.entity.Comment;
import com.project.monu.domain.comment.repository.CommentLikeRepository;
import com.project.monu.domain.comment.repository.CommentRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public CommentDto create(CommentCreateRequest request) {

        Article article = articleRepository.findById(request.articleId())
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
        Comment comment = commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(requestUserId)) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.updateContent(request.content());

        return new CommentDto(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getUser().getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                0L,
                false,
                comment.getCreatedAt()
        );
    }

    @Transactional
    @Override
    public void delete(UUID commentId, UUID requestUserId) {

        Comment comment = commentRepository.findActiveById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(requestUserId)) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        // 댓글 목록에서는 숨기되 관련 이력은 유지하고, 기사 정렬용 댓글 수만 동기화합니다.
        comment.delete();
        comment.getArticle().decreaseCommentCount();
    }

    @Override
    public void hardDelete(UUID commentId) {
    }

    @Override
    public CommentLikeDto like(UUID commentId, UUID requestUserId) {
        return null;
    }

    @Override
    public void unlike(UUID commentId, UUID requestUserId) {
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(UUID articleId) {

        List<Comment> comments = commentRepository.findAllActiveByArticleId(articleId);

        return comments.stream()
                .map(comment -> new CommentDto(
                        comment.getId(),
                        comment.getArticle().getId(),
                        comment.getUser().getId(),
                        comment.getUser().getNickname(),
                        comment.getContent(),
                        0L,
                        false,
                        comment.getCreatedAt()
                ))
                .toList();
    }
}
