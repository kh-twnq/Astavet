package com.astavet.shop.service.impl;

import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Product;
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
    public ProductServiceImpl(ProductRepository repository) { this.repository = repository; }
    @Override
    public List<Product> list() { return repository.listActive(); }
    @Override
    public Product find(UUID id) { return repository.find(id); }
    @Override
    @Transactional
    public List<Product> lockProducts(List<CartLine> lines) {
        return lines.stream().map(CartLine::productId).distinct().sorted()
                .map(repository::lock).toList();
    }
    @Override
    @Transactional
    public void reserve(List<CartLine> lines, List<Product> products) {
        for (Product product : products) {
            int quantity = lines.stream().filter(line -> line.productId().equals(product.id()))
                    .mapToInt(CartLine::quantity).sum();
            product.requireAvailable(quantity);
            repository.setStock(product.id(), product.stock() - quantity);
        }
    }
    @Override
    @Transactional
    public void restore(List<CartLine> lines) {
        for (CartLine line : lines.stream().sorted(Comparator.comparing(CartLine::productId)).toList()) {
            Product product = repository.lock(line.productId());
            repository.setStock(product.id(), Math.addExact(product.stock(), line.quantity()));
        }
    }
}
