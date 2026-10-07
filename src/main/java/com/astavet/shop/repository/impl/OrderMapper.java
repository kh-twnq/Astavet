package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Customer;
import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderLine;
import com.astavet.shop.entity.OrderEntity;
import com.astavet.shop.entity.OrderLineEntity;
import java.util.UUID;

final class OrderMapper {
  private OrderMapper() {}

  static Order model(OrderEntity entity) {
    return new Order(
        entity.id,
        entity.cartId,
        entity.idempotencyKey,
        entity.requestHash,
        new Customer(
            entity.customerName,
            entity.email,
            entity.phone,
            entity.address,
            entity.city,
            entity.postcode,
            entity.state),
        entity.lines.stream()
            .map(line -> new OrderLine(line.productId, line.name, line.unitPrice, line.quantity))
            .toList(),
        entity.subtotal,
        entity.shipping,
        entity.total,
        entity.currency,
        entity.status,
        entity.createdAt,
        entity.updatedAt,
        entity.accountId,
        entity.couponCode,
        entity.discount);
  }

  static OrderEntity entity(Order order) {
    OrderEntity e = new OrderEntity();
    e.id = order.id();
    e.cartId = order.cartId();
    e.idempotencyKey = order.idempotencyKey();
    e.requestHash = order.requestHash();
    Customer c = order.customer();
    e.customerName = c.name();
    e.email = c.email();
    e.phone = c.phone();
    e.address = c.address();
    e.city = c.city();
    e.postcode = c.postcode();
    e.state = c.state();
    e.subtotal = order.subtotal();
    e.shipping = order.shipping();
    e.total = order.total();
    e.currency = order.currency();
    e.accountId = order.accountId();
    e.couponCode = order.couponCode();
    e.discount = order.discount();
    e.grossTotal = order.subtotal().add(order.shipping());
    e.status = order.status();
    e.createdAt = order.createdAt();
    e.updatedAt = order.updatedAt();
    order
        .lines()
        .forEach(
            line -> {
              OrderLineEntity item = new OrderLineEntity();
              item.id = UUID.randomUUID();
              item.productId = line.productId();
              item.name = line.name();
              item.unitPrice = line.unitPrice();
              item.quantity = line.quantity();
              e.lines.add(item);
            });
    return e;
  }
}
