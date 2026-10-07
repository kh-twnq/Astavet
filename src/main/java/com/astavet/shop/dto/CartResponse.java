package com.astavet.shop.dto;

import com.astavet.shop.domain.Quote;
import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
    List<OrderLineResponse> lines,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal total,
    String currency,
    String fingerprint,
    String couponCode,
    BigDecimal discount,
    String couponMessage) {
  public static CartResponse from(Quote quote) {
    return new CartResponse(
        quote.lines().stream().map(OrderLineResponse::from).toList(),
        quote.subtotal(),
        quote.shipping(),
        quote.total(),
        quote.currency(),
        quote.fingerprint(),
        quote.couponCode(),
        quote.discount(),
        quote.couponMessage());
  }
}
