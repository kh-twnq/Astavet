package com.astavet.dto.response.order;

import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        UUID variantId,
        String productName,
        String variantName,
        String sku,
        long unitPrice,
        int quantity,
        long lineTotal) {
}
