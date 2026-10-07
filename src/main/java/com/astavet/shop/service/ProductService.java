package com.astavet.shop.service;

import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Product;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    default List<Product> list() { return list(0, ""); }
    List<Product> list(int page, String query);
    List<Product> findAll(List<UUID> ids);
    List<Product> selected(List<UUID> ids);
    Product findActiveBySlug(String slug);
    Product find(UUID id);
    List<Product> lockProducts(List<CartLine> lines);
    void reserve(List<CartLine> lines, List<Product> products);
    void restore(List<CartLine> lines);
}
