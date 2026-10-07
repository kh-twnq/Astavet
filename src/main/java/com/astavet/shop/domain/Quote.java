package com.astavet.shop.domain;

import java.math.BigDecimal;
import java.util.List;

public record Quote(
    List<OrderLine> lines,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal total,
    String currency,
    String fingerprint,
    String couponCode,
    BigDecimal discount,
    String couponMessage) {
  public Quote(
      List<OrderLine> lines,
      BigDecimal subtotal,
      BigDecimal shipping,
      BigDecimal total,
      String currency,
      String fingerprint) {
    this(
        lines,
        subtotal,
        shipping,
        total,
        currency,
        fingerprint,
        null,
        new BigDecimal("0.00"),
        null);
  }
}
