package com.astavet.shop.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(UUID id, String slug, String name, String description,
                      BigDecimal price, String currency, int stock, boolean active) {
    public void requireAvailable(int quantity) {
        if (!active || quantity < 1 || quantity > 99 || stock < quantity) {
            throw new ShopException(409, "The requested quantity is unavailable. Please update your cart.");
        }
    }
}
