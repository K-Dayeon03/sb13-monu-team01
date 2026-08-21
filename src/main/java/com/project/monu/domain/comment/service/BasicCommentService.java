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


        // 공통 예외 구조 맞출 때 ArticleNotFoundException, UserNotFoundException 등으로 바꿀 것
        Article article = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다."));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Comment comment = new Comment(article, user, request.content());

        Comment savedComment = commentRepository.save(comment);

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

    @Override
    public CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
        return null;
    }

    @Override
    public void delete(UUID commentId, UUID requestUserId) {
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