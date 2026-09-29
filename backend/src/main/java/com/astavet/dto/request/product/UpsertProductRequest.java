package com.astavet.dto.request.product;

import com.astavet.entity.product.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpsertProductRequest(
        @NotBlank @Size(max = 180) String slug,
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String shortDescription,
        @Size(max = 20000) String description,
        @NotNull ProductStatus status,
        List<@Valid ImageRequest> images,
        @NotEmpty List<@Valid VariantRequest> variants) {
}
