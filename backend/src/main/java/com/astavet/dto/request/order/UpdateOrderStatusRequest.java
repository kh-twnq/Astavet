package com.astavet.dto.request.order;

import com.astavet.entity.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
