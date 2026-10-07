package com.astavet.shop.entity;

import com.astavet.shop.domain.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class OrderEntity {
    @Id public UUID id;
    @Column(nullable = false) public UUID cartId;
    @Column(nullable = false) public UUID idempotencyKey;
    @Column(nullable = false, length = 64) public String requestHash;
    @Column(nullable = false, length = 100) public String customerName;
    @Column(nullable = false, length = 254) public String email;
    @Column(nullable = false, length = 30) public String phone;
    @Column(nullable = false, length = 300) public String address;
    @Column(nullable = false, length = 100) public String city;
    @Column(nullable = false, length = 10) public String postcode;
    @Column(nullable = false, length = 3) public String state;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal subtotal;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal shipping;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal total;
    @Column(nullable = false, length = 3) public String currency;
    @Column(nullable = false, length = 3) public String paymentMethod = "COD";
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20) public OrderStatus status;
    @Column(nullable = false) public Instant createdAt;
    @Column(nullable = false) public Instant updatedAt;
    public UUID accountId;
    @Column(length = 30) public String couponCode;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal discount;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal grossTotal;
    @Version public long version;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", nullable = false)
    public List<OrderLineEntity> lines = new ArrayList<>();
}
