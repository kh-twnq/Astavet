package com.astavet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "unit_price", nullable = false)
    private long unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "line_total", nullable = false)
    private long lineTotal;

    protected OrderItem() {
    }

    public OrderItem(CustomerOrder order, ProductVariant variant, int quantity) {
        this.order = order;
        this.productId = variant.getProduct().getId();
        this.variantId = variant.getId();
        this.productName = variant.getProduct().getName();
        this.variantName = variant.getName();
        this.sku = variant.getSku();
        this.unitPrice = variant.getPrice();
        this.quantity = quantity;
        this.lineTotal = Math.multiplyExact(unitPrice, quantity);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getVariantId() {
        return variantId;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariantName() {
        return variantName;
    }

    public String getSku() {
        return sku;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getLineTotal() {
        return lineTotal;
    }
}
