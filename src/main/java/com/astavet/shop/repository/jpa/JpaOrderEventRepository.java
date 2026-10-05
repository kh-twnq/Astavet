package com.astavet.shop.repository.jpa;

import com.astavet.shop.repository.entity.OrderEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderEventRepository extends JpaRepository<OrderEventEntity, UUID> {}
