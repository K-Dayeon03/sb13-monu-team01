package com.project.monu.domain.interest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record InterestUpdateRequest(
        @NotEmpty
        @Size(min = 1, max = 10)
        List<@NotBlank String> keywords
) {
}