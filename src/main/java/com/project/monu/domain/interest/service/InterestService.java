package com.project.monu.domain.interest.service;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.request.InterestSearchCondition;
import com.project.monu.domain.interest.dto.request.InterestSortType;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.exception.InterestDuplicateException;
import com.project.monu.domain.interest.repository.InterestRepository;
import com.project.monu.domain.interest.util.InterestSimilarityCalculator;
import com.project.monu.global.dto.CursorPageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterestService {

    private final InterestRepository interestRepository;

    public InterestService(InterestRepository interestRepository) {
        this.interestRepository = interestRepository;
    }

    @Transactional
    public InterestDto register(InterestRegisterRequest request) {
        boolean isDuplicate = interestRepository.findAllNames().stream()
                .anyMatch(existingName -> InterestSimilarityCalculator.isSimilar(existingName, request.name()));

        if (isDuplicate) {
            throw new InterestDuplicateException(request.name());
        }

        Interest interest = Interest.create(request.name());
        request.keywords().forEach(keyword -> interest.addKeyword(Keyword.of(keyword)));

        Interest saved = interestRepository.save(interest);

        return toDto(saved, false);
    }

    private InterestDto toDto(Interest interest, boolean subscribedByMe) {
        List<String> keywordNames = interest.getKeywords().stream()
                .map(Keyword::getKeyword)
                .toList();

        return new InterestDto(
                interest.getId(),
                interest.getName(),
                keywordNames,
                interest.getSubscriberCount(),
                subscribedByMe
        );
    }

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Transactional(readOnly = true)
    public CursorPageResponse<InterestDto> getInterests(InterestSearchCondition condition) {
        int size = normalizeSize(condition.size());
        InterestSearchCondition normalizedCondition = new InterestSearchCondition(
                condition.keyword(),
                condition.sortType(),
                condition.nextCursor(),
                size
        );

        List<Interest> interests = interestRepository.searchByCursor(normalizedCondition);

        boolean hasNext = interests.size() > size;
        if (hasNext) {
            interests = interests.subList(0, size);
        }

        List<InterestDto> content = interests.stream()
                .map(interest -> toDto(interest, false))
                .toList();

        Interest lastInterest = interests.isEmpty() ? null : interests.get(interests.size() - 1);

        return new CursorPageResponse<>(
                content,
                hasNext ? createNextCursor(lastInterest, normalizedCondition.sortType()) : null,
                null,
                size,
                interestRepository.countByCondition(normalizedCondition),
                hasNext
        );
    }

    private String createNextCursor(Interest interest, InterestSortType sortType) {
        if (interest == null) {
            return null;
        }

        InterestSortType resolvedSortType = sortType == null
                ? InterestSortType.SUBSCRIBER_COUNT
                : sortType;

        return switch (resolvedSortType) {
            case SUBSCRIBER_COUNT -> interest.getSubscriberCount() + "_" + interest.getId();
            case NAME -> interest.getName() + "_" + interest.getId();
        };
    }

    private int normalizeSize(int requestedSize) {
        if (requestedSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(requestedSize, MAX_PAGE_SIZE);
    }

}