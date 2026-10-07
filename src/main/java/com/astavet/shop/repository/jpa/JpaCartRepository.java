package com.astavet.shop.repository.jpa;

import com.astavet.shop.entity.CartEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaCartRepository extends JpaRepository<CartEntity, UUID> {
  @Modifying
  @Query(value = "insert into carts (id) values (:id) on conflict do nothing", nativeQuery = true)
  void ensure(@Param("id") UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from CartEntity c where c.id = :id")
  Optional<CartEntity> lock(@Param("id") UUID id);
}
