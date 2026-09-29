package com.astavet.dto.response;

import java.util.UUID;

public record VariantResponse(UUID id, String name, String sku, long price, int stockQuantity, boolean active) {
}
