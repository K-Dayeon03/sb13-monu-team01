package com.project.monu.domain.interest.service;

import com.project.monu.domain.interest.dto.request.InterestRegisterRequest;
import com.project.monu.domain.interest.dto.response.InterestDto;
import com.project.monu.domain.interest.entity.Interest;
import com.project.monu.domain.interest.entity.Keyword;
import com.project.monu.domain.interest.exception.InterestDuplicateException;
import com.project.monu.domain.interest.repository.InterestRepository;
import com.project.monu.domain.interest.util.InterestSimilarityCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.monu.domain.interest.exception.InterestDuplicateException;
import com.project.monu.domain.interest.util.InterestSimilarityCalculator;

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
}