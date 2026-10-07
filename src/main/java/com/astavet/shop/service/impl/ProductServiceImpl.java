package com.astavet.shop.service.impl;

import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Product;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.repository.ProductRepository;
import com.astavet.shop.service.ProductService;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
  private final ProductRepository repository;

  public ProductServiceImpl(ProductRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Product> list(int page, String query) {
    if (page < 0 || page > 100000) {
      throw new ShopException(400, "Invalid page number.");
    }
    if (query == null || query.length() > 100) {
      throw new ShopException(400, "Search must contain at most 100 characters.");
    }
    return repository.listActive(page, query.trim());
  }

  @Override
  public List<Product> findAll(List<UUID> ids) {
    if (ids == null || ids.size() > 100 || ids.stream().anyMatch(java.util.Objects::isNull)) {
      throw new ShopException(400, "Request at most 100 product IDs.");
    }
    return repository.findAll(ids);
  }

  @Override
  public List<Product> selected(List<UUID> ids) {
    if (ids == null
        || ids.isEmpty()
        || ids.size() > 50
        || ids.stream().anyMatch(java.util.Objects::isNull)) {
      throw new ShopException(400, "Request 1 to 50 product IDs.");
    }
    return repository.findAll(ids.stream().distinct().toList()).stream()
        .filter(Product::active)
        .toList();
  }

  @Override
  public Product findActiveBySlug(String slug) {
    if (slug == null || slug.length() > 100) {
      throw new ShopException(400, "Invalid product slug.");
    }
    return repository.findActiveBySlug(slug);
  }

  @Override
  public Product find(UUID id) {
    return repository.find(id);
  }

  @Override
  @Transactional
  public List<Product> lockProducts(List<CartLine> lines) {
    return lines.stream()
        .map(CartLine::productId)
        .distinct()
        .sorted()
        .map(repository::lock)
        .toList();
  }

  @Override
  @Transactional
  public void reserve(List<CartLine> lines, List<Product> products) {
    for (Product product : products) {
      int quantity =
          lines.stream()
              .filter(line -> line.productId().equals(product.id()))
              .mapToInt(CartLine::quantity)
              .sum();
      product.requireAvailable(quantity);
      repository.setStock(product.id(), product.stock() - quantity);
    }
  }

  @Override
  @Transactional
  public void restore(List<CartLine> lines) {
    for (CartLine line :
        lines.stream().sorted(Comparator.comparing(CartLine::productId)).toList()) {
      Product product = repository.lock(line.productId());
      repository.setStock(product.id(), Math.addExact(product.stock(), line.quantity()));
    }
  }
}
