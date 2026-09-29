package com.astavet.dto.response;

import com.astavet.entity.OrderStatus;
import java.time.Instant;

public record StatusHistoryResponse(
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String changedBy,
        Instant createdAt) {
}
