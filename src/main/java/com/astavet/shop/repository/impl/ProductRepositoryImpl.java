package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.StockAdjustment;
import com.astavet.shop.entity.ProductEntity;
import com.astavet.shop.entity.StockAdjustmentEntity;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.repository.ProductRepository;
import com.astavet.shop.repository.jpa.JpaProductRepository;
import com.astavet.shop.repository.jpa.JpaStockAdjustmentRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
  private final JpaProductRepository jpa;
  private final JpaStockAdjustmentRepository adjustments;

  public ProductRepositoryImpl(JpaProductRepository jpa, JpaStockAdjustmentRepository adjustments) {
    this.jpa = jpa;
    this.adjustments = adjustments;
  }

  @Override
  public List<Product> listActive(int page, String query) {
    return jpa
        .findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAscIdAsc(
            query, PageRequest.of(page, 25))
        .stream()
        .map(this::map)
        .toList();
  }

  @Override
  public List<Product> findAll(List<UUID> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    Map<UUID, Product> found =
        jpa.findAllById(ids).stream()
            .map(this::map)
            .collect(Collectors.toMap(Product::id, Function.identity()));
    return ids.stream()
        .map(
            id -> {
              Product product = found.get(id);
              if (product == null) {
                throw new ShopException(404, "Product not found.");
              }
              return product;
            })
        .toList();
  }

  @Override
  public Product findActiveBySlug(String slug) {
    return map(
        jpa.findBySlugAndActiveTrue(slug)
            .orElseThrow(() -> new ShopException(404, "Product not found.")));
  }

  @Override
  public Product find(UUID id) {
    return map(jpa.findById(id).orElseThrow(() -> new ShopException(404, "Product not found.")));
  }

  @Override
  public Product lock(UUID id) {
    return map(jpa.lock(id).orElseThrow(() -> new ShopException(404, "Product not found.")));
  }

  @Override
  public void setStock(UUID id, int stock) {
    ProductEntity entity = jpa.findById(id).orElseThrow();
    entity.stock = stock;
  }

  @Override
  public List<Product> listAll(int page) {
    return jpa.findAllByOrderByNameAscIdAsc(PageRequest.of(page, 25)).stream()
        .map(this::map)
        .toList();
  }

  @Override
  public Product save(Product product) {
    ProductEntity e = new ProductEntity();
    e.id = product.id();
    e.slug = product.slug();
    e.name = product.name();
    e.description = product.description();
    e.price = product.price();
    e.currency = product.currency();
    e.stock = product.stock();
    e.active = product.active();
    e.imagePath = product.imagePath();
    e.version = product.version();
    return map(jpa.saveAndFlush(e));
  }

  @Override
  public Optional<StockAdjustment> findAdjustment(UUID id, UUID key) {
    return adjustments.findByProductIdAndOperationId(id, key).map(this::mapAdjustment);
  }

  @Override
  public StockAdjustment saveAdjustment(StockAdjustment stockAdjustment) {
    StockAdjustmentEntity e = new StockAdjustmentEntity();
    e.id = UUID.randomUUID();
    e.productId = stockAdjustment.productId();
    e.operationId = stockAdjustment.operationId();
    e.delta = stockAdjustment.delta();
    e.reason = stockAdjustment.reason();
    e.actor = stockAdjustment.actor();
    e.resultingStock = stockAdjustment.resultingStock();
    e.createdAt = stockAdjustment.createdAt();
    return mapAdjustment(adjustments.saveAndFlush(e));
  }

  @Override
  public List<StockAdjustment> adjustments(UUID id) {
    return adjustments.findTop25ByProductIdOrderByCreatedAtDesc(id).stream()
        .map(this::mapAdjustment)
        .toList();
  }

  private StockAdjustment mapAdjustment(StockAdjustmentEntity entity) {
    return new StockAdjustment(
        entity.productId,
        entity.operationId,
        entity.delta,
        entity.reason,
        entity.actor,
        entity.resultingStock,
        entity.createdAt);
  }

  private Product map(ProductEntity entity) {
    return new Product(
        entity.id,
        entity.slug,
        entity.name,
        entity.description,
        entity.price,
        entity.currency,
        entity.stock,
        entity.active,
        entity.imagePath,
        entity.version);
  }
}
