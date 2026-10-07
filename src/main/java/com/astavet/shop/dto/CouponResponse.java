package com.astavet.shop.dto;

import com.astavet.shop.domain.Coupon;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
    UUID id,
    String code,
    BigDecimal amount,
    BigDecimal minimumSubtotal,
    Instant expiresAt,
    int maxUses,
    int uses,
    boolean active,
    long version) {
  public static CouponResponse from(Coupon coupon) {
    return new CouponResponse(
        coupon.id(),
        coupon.code(),
        coupon.amount(),
        coupon.minimumSubtotal(),
        coupon.expiresAt(),
        coupon.maxUses(),
        coupon.uses(),
        coupon.active(),
        coupon.version());
  }
}
