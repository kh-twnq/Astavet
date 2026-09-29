package com.astavet.mapper;

import com.astavet.dto.response.ImageResponse;
import com.astavet.dto.response.ProductResponse;
import com.astavet.dto.response.VariantResponse;
import com.astavet.entity.Product;
import com.astavet.entity.ProductImage;
import java.util.Comparator;
import java.util.List;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product, boolean includeInactiveVariants) {
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
