package com.astavet.shop.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Order(
    UUID id,
    UUID cartId,
    UUID idempotencyKey,
    String requestHash,
    Customer customer,
    List<OrderLine> lines,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal total,
    String currency,
    OrderStatus status,
    Instant createdAt,
    Instant updatedAt,
    UUID accountId,
    String couponCode,
    BigDecimal discount) {
  public Order(
      UUID id,
      UUID cartId,
      UUID key,
      String hash,
      Customer customer,
      List<OrderLine> lines,
      BigDecimal subtotal,
      BigDecimal shipping,
      BigDecimal total,
      String currency,
      OrderStatus status,
      Instant createdAt,
      Instant updatedAt) {
    this(
        id,
        cartId,
        key,
        hash,
        customer,
        lines,
        subtotal,
        shipping,
        total,
        currency,
        status,
        createdAt,
        updatedAt,
        null,
        null,
        new BigDecimal("0.00"));
  }
}
