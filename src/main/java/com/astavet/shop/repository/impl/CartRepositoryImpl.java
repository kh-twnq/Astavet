package com.astavet.shop.repository.impl;

import com.astavet.shop.domain.Cart;
import com.astavet.shop.domain.CartLine;
import com.astavet.shop.repository.CartRepository;
import com.astavet.shop.repository.entity.CartEntity;
import com.astavet.shop.repository.entity.CartLineEntity;
import com.astavet.shop.repository.jpa.JpaCartRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class CartRepositoryImpl implements CartRepository {
    private final JpaCartRepository jpa;
    public CartRepositoryImpl(JpaCartRepository jpa) { this.jpa = jpa; }
    @Override
    public Cart lock(UUID id) {
        jpa.ensure(id);
        CartEntity entity = jpa.lock(id).orElseThrow();
        return new Cart(id, entity.lines.stream().map(line -> new CartLine(line.productId, line.quantity)).toList(), entity.couponCode);
    }
    @Override public void setCoupon(UUID id, String code) { jpa.findById(id).orElseThrow().couponCode = code; }
    @Override
    public void replaceLines(UUID id, List<CartLine> lines) {
        CartEntity entity = jpa.findById(id).orElseThrow();
        entity.lines.clear();
        lines.forEach(line -> entity.lines.add(new CartLineEntity(line.productId(), line.quantity())));
    }
}
