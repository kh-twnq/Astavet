package com.astavet.mapper.order;

import com.astavet.dto.response.order.OrderItemResponse;
import com.astavet.dto.response.order.OrderResponse;
import com.astavet.dto.response.order.PaymentHistoryResponse;
import com.astavet.dto.response.order.StatusHistoryResponse;
import com.astavet.entity.order.CustomerOrder;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getVariantId(), item.getProductName(),
                        item.getVariantName(), item.getSku(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal()))
                .toList();
        List<StatusHistoryResponse> history = order.getHistory().stream()
                .map(item -> new StatusHistoryResponse(item.getPreviousStatus(), item.getNewStatus(),
                        item.getChangedBy(), item.getCreatedAt()))
                .toList();
        List<PaymentHistoryResponse> paymentHistory = order.getPaymentHistory().stream()
                .map(item -> new PaymentHistoryResponse(item.getPreviousStatus(), item.getNewStatus(),
                        item.getChangedBy(), item.getCreatedAt()))
                .toList();
        return new OrderResponse(order.getId(), order.getOrderCode(), order.getCustomerName(), order.getPhone(),
                order.getAddress(), order.getNote(), order.getSubtotal(), order.getShippingFee(), order.getTotal(),
                order.getPaymentMethod(), order.getPaymentStatus(), order.getStatus(), items, history, paymentHistory,
                order.getCreatedAt(), order.getUpdatedAt());
    }
}
