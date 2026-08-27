package com.project.monu.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.comment.dto.CommentDto;
import com.project.monu.domain.comment.dto.request.CommentCreateRequest;
import com.project.monu.domain.comment.dto.request.CommentSearchCondition;
import com.project.monu.domain.comment.dto.request.CommentSortType;
import com.project.monu.domain.comment.dto.request.CommentUpdateRequest;
import com.project.monu.domain.comment.entity.Comment;
import com.project.monu.domain.comment.repository.CommentLikeRepository;
import com.project.monu.domain.comment.repository.CommentQueryResult;
import com.project.monu.domain.comment.repository.CommentRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.dto.CursorPageResponse;
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
import org.springframework.data.domain.Sort;

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

        CommentCreateRequest request = new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

        Article article = mock(Article.class);
        User user = mock(User.class);
        Comment savedComment = mock(Comment.class);

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId)).thenReturn(Optional.of(article));
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
        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.articleId()).isEqualTo(articleId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.userNickname()).isEqualTo("댓글테스터");
        assertThat(result.content()).isEqualTo("댓글 등록 테스트입니다.");
        assertThat(result.likeCount()).isZero();
        assertThat(result.likedByMe()).isFalse();
        assertThat(result.createdAt()).isEqualTo(createdAt);

        verify(articleRepository).findByIdAndDeletedAtIsNull(articleId);
        verify(userRepository).findById(userId);
        verify(commentRepository).save(any(Comment.class));
        verify(article).increaseCommentCount();
    }

    @Test
    void 기사가_없으면_댓글_등록에_실패한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.create(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ARTICLE_NOT_FOUND);
    }

    @Test
    void 사용자가_없으면_댓글_등록에_실패한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(articleId, userId, "댓글 등록 테스트입니다.");

        Article article = mock(Article.class);

        when(articleRepository.findByIdAndDeletedAtIsNull(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.create(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
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

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
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
        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.articleId()).isEqualTo(articleId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.content()).isEqualTo("수정된 댓글입니다.");

        verify(comment).updateContent("수정된 댓글입니다.");
    }

    @Test
    void 댓글이_없으면_수정에_실패한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다.");

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.update(commentId, userId, request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
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

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(writerId);

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.update(commentId, requestUserId, request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_ACCESS_DENIED);
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

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
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

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.delete(commentId, userId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void 작성자가_아니면_댓글_삭제에_실패한다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID writerId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        Comment comment = mock(Comment.class);
        User user = mock(User.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(writerId);

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.delete(commentId, requestUserId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_ACCESS_DENIED);
        verify(comment, never()).delete();
    }

    @Test
    void 삭제된_댓글은_수정할_수_없다() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다.");

        Comment comment = mock(Comment.class);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(comment.getDeletedAt()).thenReturn(Instant.now());

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.update(commentId, userId, request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    void 기사별_댓글_목록을_조회한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-24T00:00:00Z");

        Comment comment = mock(Comment.class);
        Article article = mock(Article.class);
        User user = mock(User.class);

        CommentSearchCondition condition = new CommentSearchCondition(
                articleId,
                CommentSortType.CREATED_AT,
                Sort.Direction.DESC,
                null,
                null,
                10,
                requestUserId
        );

        CommentQueryResult queryResult = new CommentQueryResult(comment, 0L, false);

        when(commentRepository.searchByCursor(condition)).thenReturn(List.of(queryResult));
        when(commentRepository.countByCondition(condition)).thenReturn(1L);
        when(comment.getId()).thenReturn(commentId);
        when(comment.getArticle()).thenReturn(article);
        when(comment.getUser()).thenReturn(user);
        when(comment.getContent()).thenReturn("댓글 내용입니다.");
        when(comment.getCreatedAt()).thenReturn(createdAt);
        when(article.getId()).thenReturn(articleId);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("댓글테스터");

        // when
        CursorPageResponse<CommentDto> result =
                commentService.getComments(articleId, "createdAt", "DESC", null, null, 10, requestUserId);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo(commentId);
        assertThat(result.content().get(0).articleId()).isEqualTo(articleId);
        assertThat(result.content().get(0).userId()).isEqualTo(userId);
        assertThat(result.content().get(0).userNickname()).isEqualTo("댓글테스터");
        assertThat(result.content().get(0).content()).isEqualTo("댓글 내용입니다.");
        assertThat(result.content().get(0).likeCount()).isZero();
        assertThat(result.content().get(0).likedByMe()).isFalse();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.hasNext()).isFalse();

        verify(commentRepository).searchByCursor(condition);
        verify(commentRepository).countByCondition(condition);
    }

    @Test
    void 다음_페이지가_있으면_커서와_after를_반환한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();

        Comment first = mock(Comment.class);
        Comment second = mock(Comment.class);
        Comment third = mock(Comment.class);
        Article article = mock(Article.class);
        User user = mock(User.class);

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        Instant firstCreatedAt = Instant.parse("2026-08-24T03:00:00Z");
        Instant secondCreatedAt = Instant.parse("2026-08-24T02:00:00Z");
        CommentSearchCondition condition = new CommentSearchCondition(
                articleId,
                CommentSortType.CREATED_AT,
                Sort.Direction.DESC,
                null,
                null,
                2,
                requestUserId
        );

        when(commentRepository.searchByCursor(condition)).thenReturn(List.of(
                new CommentQueryResult(first, 1L, false),
                new CommentQueryResult(second, 2L, true),
                new CommentQueryResult(third, 0L, false)
        ));
        when(commentRepository.countByCondition(condition)).thenReturn(3L);

        when(article.getId()).thenReturn(articleId);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("댓글테스터");

        when(first.getId()).thenReturn(firstId);
        when(first.getArticle()).thenReturn(article);
        when(first.getUser()).thenReturn(user);
        when(first.getContent()).thenReturn("첫 번째 댓글");
        when(first.getCreatedAt()).thenReturn(firstCreatedAt);

        when(second.getId()).thenReturn(secondId);
        when(second.getArticle()).thenReturn(article);
        when(second.getUser()).thenReturn(user);
        when(second.getContent()).thenReturn("두 번째 댓글");
        when(second.getCreatedAt()).thenReturn(secondCreatedAt);

        // when
        CursorPageResponse<CommentDto> result =
                commentService.getComments(articleId, "createdAt", "DESC", null, null, 2, requestUserId);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.nextCursor()).isEqualTo(secondCreatedAt + "_" + secondId);
        assertThat(result.nextAfter()).isEqualTo(secondCreatedAt);
        assertThat(result.totalElements()).isEqualTo(3L);
    }

    @Test
    void 댓글_조회시_좋아요_정보를_반환한다() {
        // given
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID requestUserId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-08-24T00:00:00Z");

        Comment comment = mock(Comment.class);
        Article article = mock(Article.class);
        User user = mock(User.class);

        CommentSearchCondition condition = new CommentSearchCondition(
                articleId,
                CommentSortType.LIKE_COUNT,
                Sort.Direction.DESC,
                null,
                null,
                10,
                requestUserId
        );

        when(commentRepository.searchByCursor(condition))
                .thenReturn(List.of(new CommentQueryResult(comment, 3L, true)));
        when(commentRepository.countByCondition(condition)).thenReturn(1L);

        when(comment.getId()).thenReturn(commentId);
        when(comment.getArticle()).thenReturn(article);
        when(comment.getUser()).thenReturn(user);
        when(comment.getContent()).thenReturn("좋아요가 있는 댓글");
        when(comment.getCreatedAt()).thenReturn(createdAt);
        when(article.getId()).thenReturn(articleId);
        when(user.getId()).thenReturn(userId);
        when(user.getNickname()).thenReturn("댓글테스터");

        // when
        CursorPageResponse<CommentDto> result =
                commentService.getComments(articleId, "likeCount", "DESC", null, null, 10, requestUserId);

        // then
        assertThat(result.content().get(0).likeCount()).isEqualTo(3L);
        assertThat(result.content().get(0).likedByMe()).isTrue();
        verify(commentRepository).searchByCursor(condition);
    }

    @Test
    void 지원하지_않는_정렬_기준이면_댓글_조회에_실패한다() {
        // given
        UUID requestUserId = UUID.randomUUID();

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.getComments(null, "wrong", "DESC", null, null, 10, requestUserId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_INVALID_SORT_TYPE);
    }

    @Test
    void 지원하지_않는_정렬_방향이면_댓글_조회에_실패한다() {
        // given
        UUID requestUserId = UUID.randomUUID();

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.getComments(null, "createdAt", "WRONG", null, null, 10, requestUserId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_INVALID_SORT_DIRECTION);
    }

    @Test
    void limit이_1보다_작으면_댓글_조회에_실패한다() {
        // given
        UUID requestUserId = UUID.randomUUID();

        // when
        BusinessException exception = catchThrowableOfType(BusinessException.class,
                () -> commentService.getComments(null, "createdAt", "DESC", null, null, 0, requestUserId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COMMENT_INVALID_LIMIT);
    }
}
