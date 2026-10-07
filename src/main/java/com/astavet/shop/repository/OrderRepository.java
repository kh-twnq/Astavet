package com.astavet.shop.repository;

import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
  Optional<Order> findByKey(UUID cartId, UUID key);

  Order find(UUID id);

  Order lock(UUID id);

  List<Order> list(int page);

  Order save(Order order);

  Order transition(UUID id, OrderStatus next, String actor);

  List<Order> listByAccount(UUID accountId, int page);

  boolean hasDelivered(UUID accountId, UUID productId);
}
