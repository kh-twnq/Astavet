package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Product;
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
    public ProductRepositoryImpl(JpaProductRepository jpa) { this.jpa = jpa; }
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
    private Product map(ProductEntity entity) {
        return new Product(entity.id, entity.slug, entity.name, entity.description,
                entity.price, entity.currency, entity.stock, entity.active);
    }
}
