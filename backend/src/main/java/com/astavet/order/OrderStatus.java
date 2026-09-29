package com.astavet.order;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    NEW,
    CONFIRMED,
    PACKING,
    SHIPPING,
    DELIVERED,
    CANCELLED,
    RETURNED;

    public boolean canTransitionTo(OrderStatus target) {
        Set<OrderStatus> allowed = switch (this) {
            case NEW -> EnumSet.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> EnumSet.of(PACKING, CANCELLED);
            case PACKING -> EnumSet.of(SHIPPING, CANCELLED);
            case SHIPPING -> EnumSet.of(DELIVERED, RETURNED);
            case DELIVERED, CANCELLED, RETURNED -> EnumSet.noneOf(OrderStatus.class);
        };
        return allowed.contains(target);
    }
}

