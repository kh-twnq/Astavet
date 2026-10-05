package com.astavet.shop.service;

import com.astavet.shop.domain.CartLine;
import com.astavet.shop.domain.Product;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<Product> list();
    Product find(UUID id);
    List<Product> lockProducts(List<CartLine> lines);
    void reserve(List<CartLine> lines, List<Product> products);
    void restore(List<CartLine> lines);
}
