package com.astavet.shop.dto;
import com.astavet.shop.domain.Review;
import com.astavet.shop.domain.ReviewStatus;
import java.time.Instant;
import java.util.UUID;
public record ReviewResponse(UUID id, UUID productId, String authorName, int rating, String body, ReviewStatus status, Instant createdAt, Instant updatedAt, long version) {
    public static ReviewResponse from(Review r) { return new ReviewResponse(r.id(), r.productId(), r.authorName(), r.rating(), r.body(), r.status(), r.createdAt(), r.updatedAt(), r.version()); }
}
