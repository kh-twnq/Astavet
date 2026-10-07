package com.astavet.shop.domain;

import java.util.List;
import java.util.UUID;

public record Cart(UUID id, List<CartLine> lines, String couponCode) {
  public Cart(UUID id, List<CartLine> lines) {
    this(id, lines, null);
  }
}
