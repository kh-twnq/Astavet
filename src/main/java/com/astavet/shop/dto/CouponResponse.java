package com.astavet.shop.dto;
import com.astavet.shop.domain.Coupon;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record CouponResponse(UUID id, String code, BigDecimal amount, BigDecimal minimumSubtotal, Instant expiresAt, int maxUses, int uses, boolean active, long version) {
    public static CouponResponse from(Coupon c) { return new CouponResponse(c.id(), c.code(), c.amount(), c.minimumSubtotal(), c.expiresAt(), c.maxUses(), c.uses(), c.active(), c.version()); }
}
