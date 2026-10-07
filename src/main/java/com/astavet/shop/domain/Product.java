package com.astavet.shop.domain;

import com.astavet.shop.exception.ShopException;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(UUID id, String slug, String name, String description,
                      BigDecimal price, String currency, int stock, boolean active, String imagePath, long version) {
    public Product(UUID id, String slug, String name, String description, BigDecimal price, String currency, int stock, boolean active) {
        this(id, slug, name, description, price, currency, stock, active, slug.equals("astaxanthin-200g") ? "/assets/astavet-130g.png" : "/assets/product-placeholder.svg", 0);
    }
    public void requireAvailable(int quantity) {
        if (!active || quantity < 1 || quantity > 99 || stock < quantity) {
            throw new ShopException(409, "The requested quantity is unavailable. Please update your cart.");
        }
    }
}
