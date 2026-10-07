package com.astavet.shop.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLine(UUID productId, String name, BigDecimal unitPrice, int quantity) {
  public BigDecimal total() {
    return unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
