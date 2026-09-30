package com.astavet.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record VariantRequest(
        UUID id,
        @Min(0) Long version,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 100) String sku,
        @Min(0) long price,
        @Min(0) int stockQuantity,
        boolean active) {
}
