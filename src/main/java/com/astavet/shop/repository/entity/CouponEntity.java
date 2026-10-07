package com.astavet.shop.repository.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name = "coupons")
public class CouponEntity {
    @Id public UUID id;
    @Column(nullable = false, unique = true, length = 30) public String code;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal amount;
    @Column(nullable = false, precision = 12, scale = 2) public BigDecimal minimumSubtotal;
    @Column(nullable = false) public Instant expiresAt;
    public int maxUses;
    public int uses;
    public boolean active;
    @Version public long version;
}
