package com.project.monu.domain.interest.service;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.dto.response.SubscriptionDto;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.entity.Subscription;
import com.project.monu.domain.interest.repository.InterestRepository;
import com.project.monu.domain.interest.repository.SubscriptionRepository;
import com.project.monu.domain.interest.util.InterestSimilarityCalculator;
import com.project.monu.global.exception.BusinessException;
import com.project.monu.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InterestService {

    private final InterestRepository interestRepository;
    private final SubscriptionRepository subscriptionRepository;

    public InterestService(InterestRepository interestRepository, SubscriptionRepository subscriptionRepository) {
        this.interestRepository = interestRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public InterestDto register(InterestRegisterRequest request) {
        boolean isDuplicate = interestRepository.findAllNames().stream()
                .anyMatch(existingName -> InterestSimilarityCalculator.isSimilar(existingName, request.name()));

        if (isDuplicate) {
            throw new BusinessException(ErrorCode.INTEREST_ALREADY_EXISTS);
        }

        Interest interest = Interest.create(request.name());
        request.keywords().forEach(keyword -> interest.addKeyword(Keyword.of(keyword)));

        Interest saved = interestRepository.save(interest);

        return toDto(saved, false);
    }

    @Transactional
    public SubscriptionDto subscribe(UUID userId, UUID interestId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTEREST_NOT_FOUND));

        if (subscriptionRepository.existsByUserIdAndInterest_Id(userId, interestId)) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_ALREADY_EXISTS);
        }

        Subscription subscription = Subscription.create(userId, interest);
        Subscription saved = subscriptionRepository.save(subscription);
        interest.increaseSubscriberCount();

        return toSubscriptionDto(saved);
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

    private SubscriptionDto toSubscriptionDto(Subscription subscription) {
        Interest interest = subscription.getInterest();
        List<String> keywordNames = interest.getKeywords().stream()
                .map(Keyword::getKeyword)
                .toList();

        return new SubscriptionDto(
                subscription.getId(),
                interest.getId(),
                interest.getName(),
                keywordNames,
                interest.getSubscriberCount(),
                subscription.getCreatedAt()
        );
    }
}