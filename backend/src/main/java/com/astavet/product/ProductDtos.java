package com.astavet.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record ImageResponse(UUID id, String url, String altText, int sortOrder) {
    }

    public record VariantResponse(UUID id, String name, String sku, long price, int stockQuantity, boolean active) {
    }

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

        public static ProductResponse from(Product product, boolean includeInactiveVariants) {
            List<ImageResponse> images = product.getImages().stream()
                    .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                    .map(image -> new ImageResponse(image.getId(), image.getUrl(), image.getAltText(), image.getSortOrder()))
                    .toList();
            List<VariantResponse> variants = product.getVariants().stream()
                    .filter(variant -> includeInactiveVariants || variant.isActive())
                    .map(variant -> new VariantResponse(
                            variant.getId(), variant.getName(), variant.getSku(), variant.getPrice(),
                            variant.getStockQuantity(), variant.isActive()))
                    .toList();
            return new ProductResponse(
                    product.getId(), product.getSlug(), product.getName(), product.getShortDescription(),
                    product.getDescription(), product.getStatus(), images, variants,
                    product.getCreatedAt(), product.getUpdatedAt());
        }
    }

    public record ImageRequest(
            @NotBlank @Size(max = 1000)
            @Pattern(regexp = "^(https://|/)[^\\s]+$", message = "Ảnh phải dùng HTTPS hoặc đường dẫn nội bộ") String url,
            @Size(max = 255) String altText) {
    }

    public record VariantRequest(
            UUID id,
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 100) String sku,
            @Min(0) long price,
            @Min(0) int stockQuantity,
            boolean active) {
    }

    public record UpsertProductRequest(
            @NotBlank @Size(max = 180) String slug,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 500) String shortDescription,
            @Size(max = 20000) String description,
            @NotNull ProductStatus status,
            List<@Valid ImageRequest> images,
            @NotEmpty List<@Valid VariantRequest> variants) {
    }
}
