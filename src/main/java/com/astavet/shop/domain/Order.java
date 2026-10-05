package com.astavet.shop.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Order(UUID id, UUID cartId, UUID idempotencyKey, String requestHash,
                    Customer customer, List<OrderLine> lines, BigDecimal subtotal,
                    BigDecimal shipping, BigDecimal total, String currency,
                    OrderStatus status, Instant createdAt, Instant updatedAt) {}
