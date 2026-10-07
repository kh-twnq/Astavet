package com.astavet.shop.domain;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public record Coupon(UUID id, String code, BigDecimal amount, BigDecimal minimumSubtotal, Instant expiresAt,
                     int maxUses, int uses, boolean active, long version) {
    public boolean available(BigDecimal subtotal, Instant now) {
        return active && expiresAt.isAfter(now) && uses < maxUses && subtotal.compareTo(minimumSubtotal) >= 0;
    }
}
