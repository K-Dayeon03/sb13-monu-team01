package com.project.monu.domain.article.service;

import com.project.monu.domain.article.cursor.ArticleCursor;
import com.project.monu.domain.article.dto.response.ArticleDto;
import com.project.monu.domain.article.dto.request.ArticleSearchCondition;
import com.project.monu.domain.article.dto.request.ArticleSortType;
import com.project.monu.domain.article.dto.response.ArticleViewDto;
import com.project.monu.domain.article.entity.Article;
import com.project.monu.domain.article.entity.ArticleSource;
import com.project.monu.domain.article.entity.ArticleView;
import com.project.monu.domain.article.repository.ArticleInterestRepository;
import com.project.monu.domain.article.repository.ArticleRepository;
import com.project.monu.domain.article.repository.ArticleSourceRepository;
import com.project.monu.domain.article.repository.ArticleViewRepository;
import com.project.monu.domain.comment.repository.CommentLikeRepository;
import com.project.monu.domain.comment.repository.CommentRepository;
import com.project.monu.domain.notification.entity.NotificationResourceType;
import com.project.monu.domain.notification.repository.NotificationRepository;
import com.project.monu.domain.users.entity.User;
import com.project.monu.domain.users.repository.UserRepository;
import com.project.monu.global.dto.CursorPageResponse;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final ArticleRepository articleRepository;
    private final ArticleSourceRepository  articleSourceRepository;
    private final ArticleViewRepository articleViewRepository;
    private final ArticleInterestRepository articleInterestRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public CursorPageResponse<ArticleDto> getArticles(
            ArticleSearchCondition condition,
            UUID userId
    ) {
        validateUser(userId);

        ArticleSearchCondition normalizedCondition = normalizeCondition(condition);
        List<Article> searchedArticles = articleRepository.searchByCursor(normalizedCondition);
        PageSlice<Article> page = slicePage(searchedArticles, normalizedCondition.limit());
        List<ArticleDto> content = toArticleDtos(page.content(), userId);

        return buildPageResponse(content, page, normalizedCondition);
    }

    private void validateUser(UUID userId) {
        if (userId == null || !userRepository.existsByIdAndDeletedAtIsNull(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    /**
     * 목록 조회 조건에 기본 정렬값과 페이지 크기 제한을 적용합니다.
     *
     * <p>Controller에서 기본값을 받더라도 Service에서 한 번 더 보정해 두면,
     * 테스트나 다른 내부 호출이 들어와도 Repository는 항상 같은 규칙의 조건을 받습니다.</p>
     */
    private ArticleSearchCondition normalizeCondition(ArticleSearchCondition condition) {
        int size = normalizeSize(condition.limit());

        return new ArticleSearchCondition(
                condition.keyword(),
                condition.interestId(),
                condition.sourceIn(),
                condition.publishDateFrom(),
                condition.publishDateTo(),
                ArticleSortType.resolve(condition.orderBy()),
                condition.direction() == null ? Sort.Direction.DESC : condition.direction(),
                condition.after(),
                condition.cursor(),
                size
        );
    }

    /**
     * Repository가 size + 1개를 조회한 결과에서 실제 응답에 담을 size개만 잘라냅니다.
     *
     * <p>마지막 1개는 다음 페이지 존재 여부를 판단하기 위한 여분이라 클라이언트에는 내려주지 않습니다.</p>
     */
    private PageSlice<Article> slicePage(List<Article> articles, int size) {
        boolean hasNext = articles.size() > size;
        if (hasNext) {
            return new PageSlice<>(articles.subList(0, size), true);
        }

        return new PageSlice<>(articles, false);
    }

    /**
     * 현재 사용자 기준의 viewedByMe 값을 붙여 API 응답 DTO로 변환합니다.
     */
    private List<ArticleDto> toArticleDtos(List<Article> articles, UUID userId) {
        List<UUID> articleIds = articles.stream()
                .map(Article::getId)
                .toList();

        Set<UUID> viewedArticleIds = articleIds.isEmpty()
                ? Set.of()
                : articleViewRepository.findViewedArticleIds(userId, articleIds);

        return articles.stream()
                .map(article -> ArticleDto.from(article, viewedArticleIds.contains(article.getId())))
                .toList();
    }

    /**
     * 현재 페이지와 커서 정보를 조합해 공통 페이지 응답을 만듭니다.
     */
    private CursorPageResponse<ArticleDto> buildPageResponse(
            List<ArticleDto> content,
            PageSlice<Article> page,
            ArticleSearchCondition condition
    ) {
        Article lastArticle = page.lastItem();

        return CursorPageResponse.of(
                content,
                page.hasNext() ? ArticleCursor.createNextCursor(lastArticle, condition.orderBy()) : null,
                page.hasNext() && lastArticle != null ? lastArticle.getPublishDate() : null,
                condition.limit(),
                articleRepository.countByCondition(condition),
                page.hasNext()
        );
    }

    private int normalizeSize(int requestedSize) {
        if (requestedSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(requestedSize, MAX_PAGE_SIZE);
    }

    private record PageSlice<T>(List<T> content, boolean hasNext) {

        private T lastItem() {
            return content.isEmpty() ? null : content.get(content.size() - 1);
        }
    }

    @Transactional
    public void softDelete(UUID articleId) {
        Article article = articleRepository
                .findByIdAndDeletedAtIsNull(articleId)
                .orElseThrow(()->
                        new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        article.softDelete();
    }

    @Transactional
    public void hardDelete(UUID articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        List<UUID> commentIds = commentRepository.findIdsByArticleId(articleId);

        // FK가 걸린 하위 데이터부터 지워야 마지막 Article 삭제가 안전합니다.
        if (!commentIds.isEmpty()) {
            commentLikeRepository.deleteAllByComment_IdIn(commentIds);
            notificationRepository.deleteAllByResourceTypeAndResourceIdIn(
                    NotificationResourceType.COMMENT,
                    commentIds
            );
        }
        commentRepository.deleteAllByArticle_Id(articleId);
        articleInterestRepository.deleteAllByArticle_Id(articleId);
        articleViewRepository.deleteAllByArticle_Id(articleId);
        articleRepository.delete(article);
    }

    public List<String> getSources() {
        return articleSourceRepository.findAllByEnabledTrue().stream()
                .map(ArticleSource::getDisplayName)
                .toList();
    }

    public ArticleDto getArticle(UUID articleId, UUID userId) {
        findActiveUser(userId);

        Article article = articleRepository
                .findByIdAndDeletedAtIsNull(articleId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        boolean viewedByMe =
                articleViewRepository.existsByViewerIdAndArticleId(
                        userId,
                        articleId
                );

        return new ArticleDto(
                article.getId(),
                article.getSource().getDisplayName(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                article.getCommentCount(),
                article.getViewCount(),
                viewedByMe
        );
    }

    private User findActiveUser(UUID userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public ArticleViewDto registerView(UUID articleId, UUID userId) {
        User user = findActiveUser(userId);

        Article article = articleRepository
                .findByIdAndDeletedAtIsNull(articleId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        ArticleView existingView =
                articleViewRepository.findByViewerIdAndArticleId(
                        userId,
                        articleId
                ).orElse(null);
        if (existingView != null) {
            return ArticleViewDto.from(existingView);
        }

        article.increaseViewCount();

        ArticleView articleView = ArticleView.builder()
                .viewer(user)
                .article(article)
                .build();

        ArticleView savedView =
                articleViewRepository.save(articleView);

        return ArticleViewDto.from(savedView);
    }

}
