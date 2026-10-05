package com.astavet.shop.repository.jpa;

import com.astavet.shop.repository.entity.OrderEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {
    Optional<OrderEntity> findByCartIdAndIdempotencyKey(UUID cartId, UUID key);
    Page<OrderEntity> findAllByOrderByCreatedAtDescIdAsc(Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderEntity o where o.id = :id")
    Optional<OrderEntity> lock(@Param("id") UUID id);
}
