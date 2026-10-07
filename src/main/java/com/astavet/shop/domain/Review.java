package com.astavet.shop.domain;
import java.time.Instant;
import java.util.UUID;
public record Review(UUID id, UUID accountId, UUID productId, String authorName, int rating, String body,
                     ReviewStatus status, Instant createdAt, Instant updatedAt, long version) {}
