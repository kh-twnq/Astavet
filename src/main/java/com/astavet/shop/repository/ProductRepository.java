package com.astavet.shop.repository;

import com.astavet.shop.domain.Product;
import java.util.List;
import java.util.UUID;

public interface ProductRepository {
    List<Product> listActive();
    Product find(UUID id);
    Product lock(UUID id);
    void setStock(UUID id, int stock);
}
