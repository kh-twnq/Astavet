package com.astavet.shop.service;

import com.astavet.shop.domain.Cart;
import com.astavet.shop.domain.Quote;
import java.util.UUID;

public interface CartService {
    Quote view(UUID id);
    Quote setQuantity(UUID id, UUID productId, int quantity);
    Cart lock(UUID id);
    void clear(UUID id);
    Quote setCoupon(UUID id, String code);
}
