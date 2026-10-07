package com.astavet.shop.dto;

import com.astavet.shop.domain.Quote;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<OrderLineResponse> lines, BigDecimal subtotal, BigDecimal shipping,
                           BigDecimal total, String currency, String fingerprint, String couponCode, BigDecimal discount, String couponMessage) {
    public static CartResponse from(Quote q) {
        return new CartResponse(q.lines().stream().map(OrderLineResponse::from).toList(),
                q.subtotal(), q.shipping(), q.total(), q.currency(), q.fingerprint(), q.couponCode(), q.discount(), q.couponMessage());
    }
}
