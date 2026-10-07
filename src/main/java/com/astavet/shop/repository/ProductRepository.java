package com.astavet.shop.repository;

import com.astavet.shop.domain.Product;
import java.util.List;
import java.util.UUID;

public interface ProductRepository {
  List<Product> listActive(int page, String query);

  List<Product> findAll(List<UUID> ids);

  Product findActiveBySlug(String slug);

  Product find(UUID id);

  Product lock(UUID id);

  void setStock(UUID id, int stock);

  List<Product> listAll(int page);

  Product save(Product product);

  java.util.Optional<com.astavet.shop.domain.StockAdjustment> findAdjustment(
      UUID id, UUID operationId);

  com.astavet.shop.domain.StockAdjustment saveAdjustment(
      com.astavet.shop.domain.StockAdjustment adjustment);

  List<com.astavet.shop.domain.StockAdjustment> adjustments(UUID id);
}
