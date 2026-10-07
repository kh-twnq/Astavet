package com.astavet.shop.domain;
import java.time.Instant;
import java.util.UUID;
public record StockAdjustment(UUID productId, UUID operationId, int delta, String reason, String actor, int resultingStock, Instant createdAt) {}
