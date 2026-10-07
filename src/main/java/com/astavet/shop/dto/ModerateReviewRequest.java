package com.astavet.shop.dto;
import com.astavet.shop.domain.ReviewStatus;
import jakarta.validation.constraints.NotNull;
public record ModerateReviewRequest(@NotNull ReviewStatus expectedStatus, @NotNull ReviewStatus status, @NotNull @jakarta.validation.constraints.Min(0) Long expectedVersion) {}
