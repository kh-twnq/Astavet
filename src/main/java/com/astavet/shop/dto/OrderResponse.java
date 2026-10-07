package com.astavet.shop.dto;

import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    CustomerResponse customer,
    List<OrderLineResponse> lines,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal total,
    String currency,
    String paymentMethod,
    OrderStatus status,
    Instant createdAt,
    String couponCode,
    BigDecimal discount) {
  public static OrderResponse from(Order order) {
    return new OrderResponse(
        order.id(),
        CustomerResponse.from(order.customer()),
        order.lines().stream().map(OrderLineResponse::from).toList(),
        order.subtotal(),
        order.shipping(),
        order.total(),
        order.currency(),
        "COD",
        order.status(),
        order.createdAt(),
        order.couponCode(),
        order.discount());
  }
}
