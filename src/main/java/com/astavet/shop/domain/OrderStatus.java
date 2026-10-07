package com.astavet.shop.domain;

public enum OrderStatus {
  PLACED,
  CONFIRMED,
  SHIPPED,
  DELIVERED,
  CANCELLED;

  public boolean canTransitionTo(OrderStatus next) {
    return this == next
        || switch (this) {
          case PLACED -> next == CONFIRMED || next == CANCELLED;
          case CONFIRMED -> next == SHIPPED || next == CANCELLED;
          case SHIPPED -> next == DELIVERED;
          case DELIVERED, CANCELLED -> false;
        };
  }
}
