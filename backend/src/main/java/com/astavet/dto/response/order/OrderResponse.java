package com.astavet.dto.response.order;

import com.astavet.entity.order.OrderStatus;
import com.astavet.entity.order.PaymentMethod;
import com.astavet.entity.order.PaymentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderCode,
        String customerName,
        String phone,
        String address,
        String note,
        long subtotal,
        long shippingFee,
        long total,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        OrderStatus status,
        List<OrderItemResponse> items,
        List<StatusHistoryResponse> history,
        Instant createdAt,
        Instant updatedAt) {
}
