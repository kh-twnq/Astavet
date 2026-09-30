package com.astavet.dto.response.product;

import java.util.UUID;

public record VariantResponse(UUID id, long version, String name, String sku, long price, int stockQuantity,
        boolean active) {
}
