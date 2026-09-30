package com.astavet.mapper.product;

import com.astavet.dto.response.product.ImageResponse;
import com.astavet.dto.response.product.ProductResponse;
import com.astavet.dto.response.product.VariantResponse;
import com.astavet.entity.product.Product;
import com.astavet.entity.product.ProductImage;
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
                        variant.getId(), variant.getVersion(), variant.getName(), variant.getSku(), variant.getPrice(),
                        variant.getStockQuantity(), variant.isActive()))
                .toList();
        return new ProductResponse(
                product.getId(), product.getSlug(), product.getName(), product.getShortDescription(),
                product.getDescription(), product.getStatus(), images, variants,
                product.getCreatedAt(), product.getUpdatedAt());
    }
}
