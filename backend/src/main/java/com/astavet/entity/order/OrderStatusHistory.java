package com.astavet.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 32)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 32)
    private OrderStatus newStatus;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected OrderStatusHistory() {
    }

    public OrderStatusHistory(CustomerOrder order, OrderStatus previousStatus, OrderStatus newStatus, String changedBy) {
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
        this.createdAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
