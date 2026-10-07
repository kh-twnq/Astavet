package com.astavet.shop.dto;

import com.astavet.shop.domain.StockAdjustment;
import java.time.Instant;
import java.util.UUID;

public record StockAdjustmentResponse(
    UUID productId,
    UUID operationId,
    int delta,
    String reason,
    String actor,
    int resultingStock,
    Instant createdAt) {
  public static StockAdjustmentResponse from(StockAdjustment stockAdjustment) {
    return new StockAdjustmentResponse(
        stockAdjustment.productId(),
        stockAdjustment.operationId(),
        stockAdjustment.delta(),
        stockAdjustment.reason(),
        stockAdjustment.actor(),
        stockAdjustment.resultingStock(),
        stockAdjustment.createdAt());
  }
}
