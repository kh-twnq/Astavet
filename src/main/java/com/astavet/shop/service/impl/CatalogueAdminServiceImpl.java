package com.astavet.shop.service.impl;

import com.astavet.shop.domain.Product;
import com.astavet.shop.domain.StockAdjustment;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.repository.ProductRepository;
import com.astavet.shop.service.CatalogueAdminService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public class CatalogueAdminServiceImpl implements CatalogueAdminService {
  private final ProductRepository repository;

  public CatalogueAdminServiceImpl(ProductRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Product> list(int page) {
    requirePage(page);
    return repository.listAll(page);
  }

  @Override
  @Transactional
  public Product save(
      UUID id,
      String slug,
      String name,
      String description,
      BigDecimal price,
      boolean active,
      String imagePath,
      long expectedVersion) {
    Product previous = id == null ? null : repository.lock(id);
    if (previous != null && previous.version() != expectedVersion) {
      throw new ShopException(409, "Product changed. Refresh before saving.");
    }
    try {
      return repository.save(
          new Product(
              id == null ? UUID.randomUUID() : id,
              slug,
              name.trim(),
              description.trim(),
              price,
              "AUD",
              previous == null ? 0 : previous.stock(),
              active,
              imagePath,
              previous == null ? 0 : previous.version()));
    } catch (DataIntegrityViolationException exception) {
      throw new ShopException(409, "This product slug is already in use.");
    }
  }

  @Override
  @Transactional
  public StockAdjustment adjust(
      UUID id, UUID operationId, int delta, String reason, long expectedVersion, String actor) {
    Product product = repository.lock(id);
    Optional<StockAdjustment> previous = repository.findAdjustment(id, operationId);
    if (previous.isPresent()) {
      StockAdjustment adjustment = previous.get();
      if (adjustment.delta() != delta
          || !adjustment.reason().equals(reason.trim())
          || !adjustment.actor().equals(actor)) {
        throw new ShopException(409, "This stock operation key has already been used.");
      }
      return adjustment;
    }
    if (product.version() != expectedVersion) {
      throw new ShopException(409, "Stock changed. Refresh before adjusting.");
    }
    long stock = (long) product.stock() + delta;
    if (delta == 0 || stock < 0 || stock > 1000000) {
      throw new ShopException(
          409, "Resulting stock must be between 0 and 1000000; use a nonzero adjustment.");
    }
    repository.setStock(id, (int) stock);
    return repository.saveAdjustment(
        new StockAdjustment(
            id, operationId, delta, reason.trim(), actor, (int) stock, Instant.now()));
  }

  @Override
  public List<StockAdjustment> adjustments(UUID id) {
    return repository.adjustments(id);
  }

  private void requirePage(int page) {
    if (page < 0 || page > 100000) {
      throw new ShopException(400, "Invalid page number.");
    }
  }
}
