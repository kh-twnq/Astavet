package com.astavet.shop.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_lines")
public class OrderLineEntity {
    @Id public UUID id;
    @Column(name = "product_id", nullable = false) public UUID productId;
    @Column(nullable = false, length = 160) public String name;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal unitPrice;
    public int quantity;
}
