package com.astavet.entity.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CustomerOrderTest {

    @Test
    void deliveryDoesNotAutomaticallyConfirmCodCollection() {
        CustomerOrder order = deliveredOrder();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void confirmsCodCollectionOnlyAfterDelivery() {
        CustomerOrder order = newOrder();

        assertThatThrownBy(() -> order.updatePaymentStatus(PaymentStatus.PAID, "admin@example.com"))
                .isInstanceOf(IllegalStateException.class);

        order.transitionTo(OrderStatus.CONFIRMED, "admin@example.com");
        order.transitionTo(OrderStatus.PACKING, "admin@example.com");
        order.transitionTo(OrderStatus.SHIPPING, "admin@example.com");
        order.transitionTo(OrderStatus.DELIVERED, "admin@example.com");
        order.updatePaymentStatus(PaymentStatus.PAID, "admin@example.com");

        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getPaymentHistory()).singleElement().satisfies(history -> {
            assertThat(history.getChangedBy()).isEqualTo("admin@example.com");
            assertThat(history.getNewStatus()).isEqualTo(PaymentStatus.PAID);
            assertThat(history.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void refundsOnlyAfterTheOrderIsReturned() {
        CustomerOrder order = deliveredOrder();
        order.updatePaymentStatus(PaymentStatus.PAID, "collector@example.com");

        assertThatThrownBy(() -> order.updatePaymentStatus(PaymentStatus.REFUNDED, "admin@example.com"))
                .isInstanceOf(IllegalStateException.class);

        order.transitionTo(OrderStatus.RETURNED, "admin@example.com");
        order.updatePaymentStatus(PaymentStatus.REFUNDED, "admin@example.com");

        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(order.getPaymentHistory()).hasSize(2);
    }

    private CustomerOrder deliveredOrder() {
        CustomerOrder order = newOrder();
        order.transitionTo(OrderStatus.CONFIRMED, "admin@example.com");
        order.transitionTo(OrderStatus.PACKING, "admin@example.com");
        order.transitionTo(OrderStatus.SHIPPING, "admin@example.com");
        order.transitionTo(OrderStatus.DELIVERED, "admin@example.com");
        return order;
    }

    private CustomerOrder newOrder() {
        return new CustomerOrder("AST-TEST", "Khách hàng", "0901234567", "Địa chỉ", null,
                100_000, 30_000, "test-key");
    }
}
