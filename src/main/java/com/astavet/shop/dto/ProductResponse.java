package com.astavet.shop.dto;

import com.astavet.shop.domain.Product;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String slug,
    String name,
    String description,
    BigDecimal price,
    String currency,
    int stock,
    boolean active,
    String imagePath,
    long version) {
  public static ProductResponse from(Product product) {
    return new ProductResponse(
        product.id(),
        product.slug(),
        product.name(),
        product.description(),
        product.price(),
        product.currency(),
        product.stock(),
        product.active(),
        product.imagePath(),
        product.version());
  }
}
