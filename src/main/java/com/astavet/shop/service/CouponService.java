package com.astavet.shop.service;
import com.astavet.shop.domain.Quote;
import com.astavet.shop.domain.Coupon;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
public interface CouponService {
    Quote apply(Quote base, String code, boolean checkout);
    void redeem(String code);
    List<Coupon> list(int page);
    Coupon save(UUID id, String code, BigDecimal amount, BigDecimal minimumSubtotal, Instant expiresAt, int maxUses, boolean active, long expectedVersion);
}
