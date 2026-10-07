package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.StockAdjustment;
import com.astavet.shop.repository.entity.StockAdjustmentEntity;
import com.astavet.shop.repository.jpa.JpaStockAdjustmentRepository;
import org.springframework.data.domain.PageRequest;
import java.util.Optional;
import com.astavet.shop.domain.ShopException;
import com.astavet.shop.repository.ProductRepository;
import com.astavet.shop.repository.entity.ProductEntity;
import com.astavet.shop.repository.jpa.JpaProductRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepositoryImpl implements ProductRepository {
    private final JpaProductRepository jpa;
    private final JpaStockAdjustmentRepository adjustments;
    public ProductRepositoryImpl(JpaProductRepository jpa, JpaStockAdjustmentRepository adjustments) { this.jpa = jpa; this.adjustments = adjustments; }
    @Override
    public List<Product> listActive() { return jpa.findByActiveTrueOrderByNameAsc().stream().map(this::map).toList(); }
    @Override
    public Product find(UUID id) { return map(jpa.findById(id).orElseThrow(() -> new ShopException(404, "Product not found."))); }
    @Override
    public Product lock(UUID id) { return map(jpa.lock(id).orElseThrow(() -> new ShopException(404, "Product not found."))); }
    @Override
    public void setStock(UUID id, int stock) {
        ProductEntity entity = jpa.findById(id).orElseThrow();
        entity.stock = stock;
    }
    @Override public List<Product> listAll(int page) { return jpa.findAllByOrderByNameAscIdAsc(PageRequest.of(page, 25)).stream().map(this::map).toList(); }
    @Override public Product save(Product p) {
        ProductEntity e = new ProductEntity(); e.id = p.id(); e.slug = p.slug(); e.name = p.name(); e.description = p.description();
        e.price = p.price(); e.currency = p.currency(); e.stock = p.stock(); e.active = p.active(); e.imagePath = p.imagePath(); e.version = p.version();
        return map(jpa.saveAndFlush(e));
    }
    @Override public Optional<StockAdjustment> findAdjustment(UUID id, UUID key) { return adjustments.findByProductIdAndOperationId(id, key).map(this::mapAdjustment); }
    @Override public StockAdjustment saveAdjustment(StockAdjustment a) {
        StockAdjustmentEntity e = new StockAdjustmentEntity(); e.id = UUID.randomUUID(); e.productId = a.productId(); e.operationId = a.operationId();
        e.delta = a.delta(); e.reason = a.reason(); e.actor = a.actor(); e.resultingStock = a.resultingStock(); e.createdAt = a.createdAt();
        return mapAdjustment(adjustments.saveAndFlush(e));
    }
    @Override public List<StockAdjustment> adjustments(UUID id) { return adjustments.findTop25ByProductIdOrderByCreatedAtDesc(id).stream().map(this::mapAdjustment).toList(); }
    private StockAdjustment mapAdjustment(StockAdjustmentEntity e) { return new StockAdjustment(e.productId, e.operationId, e.delta, e.reason, e.actor, e.resultingStock, e.createdAt); }
    private Product map(ProductEntity entity) {
        return new Product(entity.id, entity.slug, entity.name, entity.description,
                entity.price, entity.currency, entity.stock, entity.active, entity.imagePath, entity.version);
    }
}
