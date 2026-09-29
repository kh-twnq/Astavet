package com.astavet.entity.order;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void permitsTheHappyPathAndCancellationBeforeShipping() {
        assertThat(OrderStatus.NEW.canTransitionTo(OrderStatus.CONFIRMED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PACKING)).isTrue();
        assertThat(OrderStatus.PACKING.canTransitionTo(OrderStatus.SHIPPING)).isTrue();
        assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.RETURNED)).isTrue();
        assertThat(OrderStatus.NEW.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void rejectsTransitionsFromTerminalStatesAndDeliveryBackwards() {
        assertThat(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.NEW)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CONFIRMED)).isFalse();
        assertThat(OrderStatus.RETURNED.canTransitionTo(OrderStatus.SHIPPING)).isFalse();
        assertThat(OrderStatus.SHIPPING.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
    }
}
