package com.astavet.shop.service;

import com.astavet.shop.domain.Coupon;
import com.astavet.shop.domain.Quote;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CouponService {
  Quote apply(Quote base, String code, boolean checkout);

  void redeem(String code);

  List<Coupon> list(int page);

  Coupon save(
      UUID id,
      String code,
      BigDecimal amount,
      BigDecimal minimumSubtotal,
      Instant expiresAt,
      int maxUses,
      boolean active,
      long expectedVersion);
}
