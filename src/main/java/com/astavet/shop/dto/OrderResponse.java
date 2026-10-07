package com.astavet.shop.dto;

import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(UUID id, CustomerResponse customer, List<OrderLineResponse> lines,
                            BigDecimal subtotal, BigDecimal shipping, BigDecimal total, String currency,
                            String paymentMethod, OrderStatus status, Instant createdAt, String couponCode, BigDecimal discount) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.id(), CustomerResponse.from(o.customer()), o.lines().stream().map(OrderLineResponse::from).toList(),
                o.subtotal(), o.shipping(), o.total(), o.currency(), "COD", o.status(), o.createdAt(), o.couponCode(), o.discount());
    }
}
