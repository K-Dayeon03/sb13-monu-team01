package com.project.monu.domain.comment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.entity.Comment;
import com.project.monu.domain.comment.repository.CommentLikeRepository;
import com.project.monu.domain.comment.repository.CommentRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BasicCommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BasicCommentService commentService;

    @Test
    void 댓글을_등록한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-24T00:00:00Z");

        CommentCreateRequest request =
                new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

        Article article = mock(Article.class);
        User user = mock(User.class);
        Comment savedComment = mock(Comment.class);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        when(article.getId()).thenReturn(articleId);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("댓글테스터");

        when(savedComment.getId()).thenReturn(commentId);
        when(savedComment.getContent()).thenReturn("댓글 등록 테스트입니다.");
        when(savedComment.getCreatedAt()).thenReturn(createdAt);

        // when
        CommentDto result = commentService.create(request);

        // then
        assertEquals(commentId, result.id());
        assertEquals(articleId, result.articleId());
        assertEquals(userId, result.userId());
        assertEquals("댓글테스터", result.userNickname());
        assertEquals("댓글 등록 테스트입니다.", result.content());
        assertEquals(0L, result.likeCount());
        assertFalse(result.likedByMe());
        assertEquals(createdAt, result.createdAt());

        verify(articleRepository).findById(articleId);
        verify(userRepository).findById(userId);
        verify(commentRepository).save(any(Comment.class));
        verify(article).increaseCommentCount();
    }

    @Test
    void 기사가_없으면_댓글_등록에_실패한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commentService.create(request)
        );

        // then
        assertEquals(ErrorCode.ARTICLE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 사용자가_없으면_댓글_등록에_실패한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CommentCreateRequest request =
                new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

        Article article = mock(Article.class);

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> commentService.create(request)
        );

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 댓글을_수정한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-24T00:00:00Z");

        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다.");

        Comment comment = mock(Comment.class);
        Article article = mock(Article.class);
        User user = mock(User.class);

        when(commentRepository.findActiveById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getUser()).thenReturn(user);
        when(comment.getArticle()).thenReturn(article);
        when(comment.getId()).thenReturn(commentId);
        when(comment.getContent()).thenReturn("수정된 댓글입니다.");
        when(comment.getCreatedAt()).thenReturn(createdAt);

        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("댓글테스터");
        when(article.getId()).thenReturn(articleId);

        // when
        CommentDto result = commentService.update(commentId, userId, request);

        // then
        assertEquals(commentId, result.id());
        assertEquals(articleId, result.articleId());
        assertEquals(userId, result.userId());
        assertEquals("수정된 댓글입니다.", result.content());

        verify(comment).updateContent("수정된 댓글입니다.");
    }

    @Test
    void 댓글이_없으면_수정에_실패한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다.");

        when(commentRepository.findActiveById(commentId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> commentService.update(commentId, userId, request));

        // then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 작성자가_아니면_댓글_수정에_실패한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID writerId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다.");

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findActiveById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(writerId);

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> commentService.update(commentId, requestUserId, request));

        // then
        assertEquals(ErrorCode.COMMENT_ACCESS_DENIED, exception.getErrorCode());
        verify(comment, never()).updateContent(anyString());
    }

    @Test
    void 댓글을_논리_삭제한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        Article article = mock(Article.class);
        User user = mock(User.class);

        when(commentRepository.findActiveById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getUser()).thenReturn(user);
        when(comment.getArticle()).thenReturn(article);
        when(user.getId()).thenReturn(userId);

        // when
        commentService.delete(commentId, userId);

        // then
        verify(comment).delete();
        verify(article).decreaseCommentCount();
    }

    @Test
    void 댓글이_없으면_삭제에_실패한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(commentRepository.findActiveById(commentId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> commentService.delete(commentId, userId));

        // then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void 작성자가_아니면_댓글_삭제에_실패한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID writerId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findActiveById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(writerId);

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                () -> commentService.delete(commentId, requestUserId));

        // then
        assertEquals(ErrorCode.COMMENT_ACCESS_DENIED, exception.getErrorCode());
        verify(comment, never()).delete();
    }

    @Test
    void 기사별_댓글_목록을_조회한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-24T00:00:00Z");

        Comment comment = mock(Comment.class);
        Article article = mock(Article.class);
        User user = mock(User.class);

        when(commentRepository.findAllActiveByArticleId(articleId)).thenReturn(List.of(comment));
        when(comment.getId()).thenReturn(commentId);
        when(comment.getArticle()).thenReturn(article);
        when(comment.getUser()).thenReturn(user);
        when(comment.getContent()).thenReturn("댓글 내용입니다.");
        when(comment.getCreatedAt()).thenReturn(createdAt);
        when(article.getId()).thenReturn(articleId);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("댓글테스터");

        // when
        List<CommentDto> result = commentService.getComments(articleId);

        // then
        assertEquals(1, result.size());
        assertEquals(commentId, result.get(0).id());
        assertEquals(articleId, result.get(0).articleId());
        assertEquals(userId, result.get(0).userId());
        assertEquals("댓글테스터", result.get(0).userNickname());
        assertEquals("댓글 내용입니다.", result.get(0).content());
        assertEquals(0L, result.get(0).likeCount());
        assertFalse(result.get(0).likedByMe());

        verify(commentRepository).findAllActiveByArticleId(articleId);
    }
}
