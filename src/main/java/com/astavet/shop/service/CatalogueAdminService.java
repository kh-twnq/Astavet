package com.astavet.shop.service;

import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.StockAdjustment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CatalogueAdminService {
  List<Product> list(int page);

  Product save(
      UUID id,
      String slug,
      String name,
      String description,
      BigDecimal price,
      boolean active,
      String imagePath,
      long expectedVersion);

  StockAdjustment adjust(
      UUID id, UUID operationId, int delta, String reason, long expectedVersion, String actor);

  List<StockAdjustment> adjustments(UUID id);
}
