package com.astavet.shop.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id public UUID id;
    @Column(nullable = false, length = 100) public String slug;
    @Column(nullable = false, length = 160) public String name;
    @Column(nullable = false, length = 2000) public String description;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal price;
    @Column(nullable = false, length = 3) public String currency;
    public int stock;
    public boolean active;
    @Column(nullable = false, length = 300) public String imagePath = "/assets/product-placeholder.svg";
    @Version public long version;
}
