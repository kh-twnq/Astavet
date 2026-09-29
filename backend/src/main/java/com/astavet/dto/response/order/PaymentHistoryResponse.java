package com.astavet.dto.response.order;

import com.astavet.entity.order.PaymentStatus;
import java.time.Instant;

public record PaymentHistoryResponse(
        PaymentStatus previousStatus,
        PaymentStatus newStatus,
        String changedBy,
        Instant createdAt) {
}
