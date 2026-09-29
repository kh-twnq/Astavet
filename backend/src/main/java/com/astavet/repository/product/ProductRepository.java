package com.astavet.repository.product;

import com.astavet.entity.product.Product;
import com.astavet.entity.product.ProductStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findAllByStatusOrderByCreatedAtDesc(ProductStatus status);

    Optional<Product> findBySlugAndStatus(String slug, ProductStatus status);

    Optional<Product> findOneById(UUID id);

    boolean existsBySlug(String slug);
}
