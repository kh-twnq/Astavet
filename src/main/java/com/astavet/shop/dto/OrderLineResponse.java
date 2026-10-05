package com.astavet.shop.dto;

import com.astavet.shop.domain.OrderLine;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineResponse(UUID productId, String name, BigDecimal unitPrice, int quantity, BigDecimal total) {
    public static OrderLineResponse from(OrderLine line) {
        return new OrderLineResponse(line.productId(), line.name(), line.unitPrice(), line.quantity(), line.total());
    }
}
