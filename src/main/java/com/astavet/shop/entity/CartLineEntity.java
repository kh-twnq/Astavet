package com.astavet.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public class CartLineEntity {
    @Column(name = "product_id", nullable = false) public UUID productId;
    public int quantity;
    public CartLineEntity() {}
    public CartLineEntity(UUID productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
