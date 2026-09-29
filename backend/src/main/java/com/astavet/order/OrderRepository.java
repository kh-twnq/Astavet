package com.astavet.order;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {

    @Query(value = "select pg_advisory_xact_lock(hashtext(:key))", nativeQuery = true)
    void lockIdempotencyKey(@Param("key") String key);

    Optional<CustomerOrder> findByIdempotencyKey(String idempotencyKey);

    Optional<CustomerOrder> findOneById(UUID id);

    Page<CustomerOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<CustomerOrder> findAllByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);
}
