package com.astavet.shop.repository;

import com.astavet.shop.domain.Cart;
import com.astavet.shop.domain.CartLine;
import java.util.List;
import java.util.UUID;

public interface CartRepository {
    Cart lock(UUID id);
    void replaceLines(UUID id, List<CartLine> lines);
}
