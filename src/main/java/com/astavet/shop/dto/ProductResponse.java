package com.astavet.shop.dto;

import com.astavet.shop.domain.Product;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(UUID id, String slug, String name, String description,
                              BigDecimal price, String currency, int stock, boolean active, String imagePath, long version) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.id(), p.slug(), p.name(), p.description(), p.price(), p.currency(), p.stock(), p.active(), p.imagePath(), p.version());
    }
}
