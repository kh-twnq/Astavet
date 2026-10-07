package com.astavet.shop.repository.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "stock_adjustments")
public class StockAdjustmentEntity {
    @Id public UUID id;
    @Column(nullable = false) public UUID productId;
    @Column(nullable = false) public UUID operationId;
    public int delta;
    @Column(nullable = false, length = 300) public String reason;
    @Column(nullable = false, length = 254) public String actor;
    public int resultingStock;
    @Column(nullable = false) public Instant createdAt;
}
