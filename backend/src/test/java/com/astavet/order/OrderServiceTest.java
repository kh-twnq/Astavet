package com.astavet.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astavet.config.StoreProperties;
import com.astavet.order.OrderDtos.CreateOrderItemRequest;
import com.astavet.order.OrderDtos.CreateOrderRequest;
import com.astavet.product.Product;
import com.astavet.product.ProductStatus;
import com.astavet.product.ProductVariant;
import com.astavet.product.ProductVariantRepository;
import com.astavet.shared.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    @Test
    void calculatesTotalsOnTheServerAndReservesStock() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductVariantRepository variantRepository = mock(ProductVariantRepository.class);
        StoreProperties properties = new StoreProperties("http://localhost:3000", 30_000,
                new StoreProperties.Admin("", ""));
        Clock clock = Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC);
        OrderService service = new OrderService(orderRepository, variantRepository, properties, clock);

        UUID variantId = UUID.randomUUID();
        Product product = mock(Product.class);
        ProductVariant variant = mock(ProductVariant.class);
        when(product.getId()).thenReturn(UUID.randomUUID());
        when(product.getName()).thenReturn("AstaVet 130g");
        when(product.getStatus()).thenReturn(ProductStatus.ACTIVE);
        when(variant.getId()).thenReturn(variantId);
        when(variant.getProduct()).thenReturn(product);
        when(variant.getName()).thenReturn("Hộp 130g");
        when(variant.getSku()).thenReturn("ASTA-130G");
        when(variant.getPrice()).thenReturn(649_000L);
        when(variant.getStockQuantity()).thenReturn(10);
        when(variant.isActive()).thenReturn(true);
        when(orderRepository.findByIdempotencyKey("checkout-1")).thenReturn(Optional.empty());
        when(variantRepository.findAllByIdForUpdate(anyCollection())).thenReturn(List.of(variant));
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateOrderRequest(
                "Nguyễn Văn A", "0901234567", "1 Đường Mẫu, TP.HCM", null, "checkout-1",
                List.of(new CreateOrderItemRequest(variantId, 2))));

        assertThat(response.subtotal()).isEqualTo(1_298_000L);
        assertThat(response.shippingFee()).isEqualTo(30_000L);
        assertThat(response.total()).isEqualTo(1_328_000L);
        assertThat(response.orderCode()).startsWith("AST-20260929-");
        assertThat(response.paymentMethod()).isEqualTo(PaymentMethod.COD);
        verify(variant).reserve(2);
    }

    @Test
    void rejectsReusingAnIdempotencyKeyForDifferentCustomerData() {
        OrderRepository orderRepository = mock(OrderRepository.class);
        ProductVariantRepository variantRepository = mock(ProductVariantRepository.class);
        StoreProperties properties = new StoreProperties("http://localhost:3000", 30_000,
                new StoreProperties.Admin("", ""));
        OrderService service = new OrderService(orderRepository, variantRepository, properties,
                Clock.fixed(Instant.parse("2026-09-29T00:00:00Z"), ZoneOffset.UTC));

        UUID variantId = UUID.randomUUID();
        OrderItem existingItem = mock(OrderItem.class);
        CustomerOrder existingOrder = mock(CustomerOrder.class);
        when(existingItem.getVariantId()).thenReturn(variantId);
        when(existingItem.getQuantity()).thenReturn(1);
        when(existingOrder.getItems()).thenReturn(List.of(existingItem));
        when(existingOrder.getCustomerName()).thenReturn("Khách cũ");
        when(existingOrder.getPhone()).thenReturn("0901234567");
        when(existingOrder.getAddress()).thenReturn("Địa chỉ cũ");
        when(orderRepository.findByIdempotencyKey("same-key")).thenReturn(Optional.of(existingOrder));

        var request = new CreateOrderRequest(
                "Khách khác", "0901234567", "Địa chỉ cũ", null, "same-key",
                List.of(new CreateOrderItemRequest(variantId, 1)));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getCode()).isEqualTo("IDEMPOTENCY_CONFLICT"));
    }
}
