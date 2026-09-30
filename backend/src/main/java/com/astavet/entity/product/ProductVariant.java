package com.astavet.entity.product;

import com.astavet.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.UUID;

@Entity
@Table(name = "product_variants")
public class ProductVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false)
    private long price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean active;

    @Version
    @Column(nullable = false)
    private long version;

    protected ProductVariant() {
    }

    public ProductVariant(Product product, String name, String sku, long price, int stockQuantity, boolean active) {
        this.product = product;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    public void update(String name, String sku, long price, int stockQuantity, boolean active) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.active = active;
    }

    public void reserve(int quantity) {
        if (quantity <= 0 || stockQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        stockQuantity -= quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Release quantity must be positive");
        }
        stockQuantity = Math.addExact(stockQuantity, quantity);
    }

    public UUID getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public long getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public long getVersion() {
        return version;
    }

    public boolean isActive() {
        return active;
    }
}
