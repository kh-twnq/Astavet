package com.astavet.shop.dto;

import com.astavet.shop.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus expectedStatus, @NotNull OrderStatus status) {}
