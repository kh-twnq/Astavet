package com.astavet.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record CreateOrderItemRequest(
            @NotNull UUID variantId,
            @jakarta.validation.constraints.Min(1) @Max(99) int quantity) {
    }

    public record CreateOrderRequest(
            @NotBlank @Size(max = 150) String customerName,
            @NotBlank @Pattern(regexp = "^(\\+?84|0)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ") String phone,
            @NotBlank @Size(max = 500) String address,
            @Size(max = 1000) String note,
            @NotBlank @Size(max = 100) String idempotencyKey,
            @NotEmpty @Size(max = 30) List<@Valid CreateOrderItemRequest> items) {
    }

    public record OrderItemResponse(
            UUID productId,
            UUID variantId,
            String productName,
            String variantName,
            String sku,
            long unitPrice,
            int quantity,
            long lineTotal) {
    }

    public record StatusHistoryResponse(
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String changedBy,
            Instant createdAt) {
    }

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

        public static OrderResponse from(CustomerOrder order) {
            List<OrderItemResponse> items = order.getItems().stream()
                    .map(item -> new OrderItemResponse(item.getProductId(), item.getVariantId(), item.getProductName(),
                            item.getVariantName(), item.getSku(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal()))
                    .toList();
            List<StatusHistoryResponse> history = order.getHistory().stream()
                    .map(item -> new StatusHistoryResponse(item.getPreviousStatus(), item.getNewStatus(),
                            item.getChangedBy(), item.getCreatedAt()))
                    .toList();
            return new OrderResponse(order.getId(), order.getOrderCode(), order.getCustomerName(), order.getPhone(),
                    order.getAddress(), order.getNote(), order.getSubtotal(), order.getShippingFee(), order.getTotal(),
                    order.getPaymentMethod(), order.getPaymentStatus(), order.getStatus(), items, history,
                    order.getCreatedAt(), order.getUpdatedAt());
        }
    }

    public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderPageResponse(
            List<OrderResponse> content,
            long totalElements,
            int totalPages,
            int number,
            int size) {
    }
}
