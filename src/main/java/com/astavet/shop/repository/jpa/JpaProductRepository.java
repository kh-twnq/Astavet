package com.astavet.shop.repository.jpa;

import com.astavet.shop.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findByActiveTrueAndNameContainingIgnoreCaseOrderByNameAscIdAsc(String name, org.springframework.data.domain.Pageable pageable);
    Optional<ProductEntity> findBySlugAndActiveTrue(String slug);
    org.springframework.data.domain.Page<ProductEntity> findAllByOrderByNameAscIdAsc(org.springframework.data.domain.Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductEntity p where p.id = :id")
    Optional<ProductEntity> lock(@Param("id") UUID id);
}
