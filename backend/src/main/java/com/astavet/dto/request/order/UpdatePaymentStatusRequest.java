package com.astavet.dto.request.order;

import com.astavet.entity.order.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePaymentStatusRequest(@NotNull PaymentStatus paymentStatus) {
}
