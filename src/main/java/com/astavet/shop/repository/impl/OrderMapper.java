package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Customer;
import com.astavet.shop.domain.Order;
import com.astavet.shop.domain.OrderLine;
import com.astavet.shop.repository.entity.OrderEntity;
import com.astavet.shop.repository.entity.OrderLineEntity;
import java.util.UUID;

final class OrderMapper {
    private OrderMapper() {}
    static Order model(OrderEntity e) {
        return new Order(e.id, e.cartId, e.idempotencyKey, e.requestHash,
                new Customer(e.customerName, e.email, e.phone, e.address, e.city, e.postcode, e.state),
                e.lines.stream().map(line -> new OrderLine(line.productId, line.name, line.unitPrice, line.quantity)).toList(),
                e.subtotal, e.shipping, e.total, e.currency, e.status, e.createdAt, e.updatedAt, e.accountId, e.couponCode, e.discount);
    }
    static OrderEntity entity(Order o) {
        OrderEntity e = new OrderEntity();
        e.id = o.id(); e.cartId = o.cartId(); e.idempotencyKey = o.idempotencyKey(); e.requestHash = o.requestHash();
        Customer c = o.customer();
        e.customerName = c.name(); e.email = c.email(); e.phone = c.phone(); e.address = c.address();
        e.city = c.city(); e.postcode = c.postcode(); e.state = c.state();
        e.subtotal = o.subtotal(); e.shipping = o.shipping(); e.total = o.total(); e.currency = o.currency();
        e.accountId = o.accountId(); e.couponCode = o.couponCode(); e.discount = o.discount(); e.grossTotal = o.subtotal().add(o.shipping());
        e.status = o.status(); e.createdAt = o.createdAt(); e.updatedAt = o.updatedAt();
        o.lines().forEach(line -> {
            OrderLineEntity item = new OrderLineEntity();
            item.id = UUID.randomUUID(); item.productId = line.productId(); item.name = line.name();
            item.unitPrice = line.unitPrice(); item.quantity = line.quantity(); e.lines.add(item);
        });
        return e;
    }
}
