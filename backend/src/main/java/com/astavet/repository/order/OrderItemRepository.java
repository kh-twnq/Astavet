package com.astavet.repository.order;

import com.astavet.entity.order.OrderItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    boolean existsByVariantId(UUID variantId);
}
