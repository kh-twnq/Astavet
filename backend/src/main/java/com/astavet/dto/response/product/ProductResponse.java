package com.astavet.dto.response.product;

import com.astavet.entity.product.ProductStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String slug,
        String name,
        String shortDescription,
        String description,
        ProductStatus status,
        List<ImageResponse> images,
        List<VariantResponse> variants,
        Instant createdAt,
        Instant updatedAt) {
}
