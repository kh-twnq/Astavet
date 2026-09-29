package com.astavet.dto.response.order;

import com.astavet.entity.order.OrderStatus;
import java.time.Instant;

public record StatusHistoryResponse(
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String changedBy,
        Instant createdAt) {
}
