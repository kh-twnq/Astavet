package com.astavet.repository;

import com.astavet.entity.Product;
import com.astavet.entity.ProductStatus;
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
