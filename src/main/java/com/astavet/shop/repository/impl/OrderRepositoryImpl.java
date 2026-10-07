package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderStatus;
import com.astavet.shop.entity.OrderEntity;
import com.astavet.shop.entity.OrderEventEntity;
import com.astavet.shop.exception.ShopException;
import com.astavet.shop.repository.OrderRepository;
import com.astavet.shop.repository.jpa.JpaOrderEventRepository;
import com.astavet.shop.repository.jpa.JpaOrderRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
  private final JpaOrderRepository jpa;
  private final JpaOrderEventRepository events;

  public OrderRepositoryImpl(JpaOrderRepository jpa, JpaOrderEventRepository events) {
    this.jpa = jpa;
    this.events = events;
  }

  @Override
  public Optional<Order> findByKey(UUID cartId, UUID key) {
    return jpa.findByCartIdAndIdempotencyKey(cartId, key).map(OrderMapper::model);
  }

  @Override
  public Order find(UUID id) {
    return OrderMapper.model(
        jpa.findById(id).orElseThrow(() -> new ShopException(404, "Order not found.")));
  }

  @Override
  public Order lock(UUID id) {
    return OrderMapper.model(
        jpa.lock(id).orElseThrow(() -> new ShopException(404, "Order not found.")));
  }

  @Override
  public List<Order> list(int page) {
    return page(jpa.pageIds(PageRequest.of(page, 25)));
  }

  @Override
  public Order save(Order order) {
    OrderEntity e = jpa.save(OrderMapper.entity(order));
    recordEvent(e, "customer");
    return OrderMapper.model(e);
  }

  @Override
  public Order transition(UUID id, OrderStatus next, String actor) {
    OrderEntity e = jpa.findById(id).orElseThrow();
    e.status = next;
    e.updatedAt = Instant.now();
    recordEvent(e, actor);
    return OrderMapper.model(e);
  }

  @Override
  public List<Order> listByAccount(UUID accountId, int page) {
    return page(jpa.pageIdsByAccount(accountId, PageRequest.of(page, 25)));
  }

  @Override
  public boolean hasDelivered(UUID accountId, UUID productId) {
    return jpa.countDelivered(accountId, productId) > 0;
  }

  private List<Order> page(List<UUID> ids) {
    if (ids.isEmpty()) {
      return List.of();
    }
    Map<UUID, OrderEntity> fetched =
        jpa.findWithLines(ids).stream()
            .collect(Collectors.toMap(order -> order.id, Function.identity()));
    return ids.stream()
        .filter(fetched::containsKey)
        .map(fetched::get)
        .map(OrderMapper::model)
        .toList();
  }

  private void recordEvent(OrderEntity order, String actor) {
    OrderEventEntity event = new OrderEventEntity();
    event.id = UUID.randomUUID();
    event.orderId = order.id;
    event.status = order.status;
    event.actor = actor;
    event.occurredAt = order.updatedAt;
    events.save(event);
  }
}
